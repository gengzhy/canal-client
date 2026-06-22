/*
 *
 *  Copyright 2026 gengzhy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.gengzhy.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import io.github.gengzhy.config.CanalProperties;
import io.github.gengzhy.dispatch.CanalEventDispatcher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Canal 客户端抽象层
 *
 * @author gengzhy
 * @version 1.0.0
 * @since 2026-06-22 10:23
 */
@Slf4j
public abstract class AbstractCanalClient implements CanalClient {
    protected CanalConnector connector;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumeThread;

    protected Thread.UncaughtExceptionHandler handler = (t, e) -> log.error("parse events has an error", e);

    @Getter
    private final CanalProperties properties;
    @Getter
    private final CanalEventDispatcher eventDispatcher;

    /**
     * 最大重连次数，防止无限重连
     */
    private static final int MAX_RECONNECT_ATTEMPTS = 10;

    /**
     * 最大退避时间（30秒）
     */
    private static final long MAX_BACKOFF_SLEEP_MS = 30000L;

    /**
     * 线程优雅停止等待超时（2秒）
     */
    private static final long THREAD_JOIN_TIMEOUT_MS = 2000L;

    /**
     * 当前重连尝试次数
     */
    private final AtomicLong reconnectAttempts = new AtomicLong(0);

    /**
     * 成功处理的批次总数（用于监控）
     */
    private final AtomicLong successBatchCount = new AtomicLong(0);

    /**
     * 失败的批次总数（用于监控）
     */
    private final AtomicLong failedBatchCount = new AtomicLong(0);

    protected AbstractCanalClient(CanalProperties properties, CanalEventDispatcher eventDispatcher) {
        this.properties = properties;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * 初始化Canal客户端
     */
    protected abstract void init();

    /**
     * 每次拉取服务端数据量
     */
    protected abstract int batchSize();

    /**
     * 开启Canal客户端执行逻辑
     */
    @Override
    public void start() {
        // 初始化Canal客户端
        this.init();
        if (running.compareAndSet(false, true)) {
            consumeThread = new Thread(this::process, "canal-consume-thread");
            consumeThread.setUncaughtExceptionHandler(handler);
            consumeThread.start();
            log.info("Canal消费线程[{}]已启动", consumeThread.getName());
        }
    }

    /**
     * 停止Canal客户端
     */
    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            // 关闭TCP连接
            if (connector != null) {
                try {
                    connector.disconnect();
                    log.info("Canal TCP长连接已关闭");
                } catch (Exception e) {
                    log.warn("关闭Canal连接时发生异常: {}", e.getMessage());
                }
            }
            // 等待消费线程正常退出
            if (consumeThread != null && consumeThread.isAlive()) {
                try {
                    consumeThread.join(THREAD_JOIN_TIMEOUT_MS);
                    if (consumeThread.isAlive()) {
                        consumeThread.interrupt();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 当前客户端是否正在运行中
     *
     * @return true-运行中/false-已停止
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 消费Canal Binlog事件的核心循环
     * 在独立线程中持续拉取并处理消息批次
     */
    protected void process() {
        long pollSleep = properties.getPollSleep().toMillis();
        long reconnectSleep = properties.getReconnectSleep().toMillis();
        long batchFailSleep = properties.getBatchFailSleep().toMillis();

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // 检查连接器状态
                if (connector == null) {
                    init();
                    if (parkOrInterrupt(reconnectSleep)) {
                        break;
                    }
                    continue;
                }

                Message message = connector.getWithoutAck(batchSize());
                long batchId = message.getId();
                int entryCount = message.getEntries().size();

                // 无新Binlog，休眠释放CPU
                if (batchId == -1 || entryCount == 0) {
                    parkOrInterrupt(pollSleep);
                    continue;
                }

                long startTime = System.currentTimeMillis();
                try {
                    // 进入全局事务事件分发处理所有表数据
                    eventDispatcher.dispatch(message);
                    // 全部处理成功，位点向前推进
                    connector.ack(batchId);
                    successBatchCount.incrementAndGet();
                } catch (Exception e) {
                    // 业务异常：数据库事务已全部回滚，执行Canal位点回退，下次重放整批
                    connector.rollback(batchId);
                    failedBatchCount.incrementAndGet();
                    long elapsed = System.currentTimeMillis() - startTime;
                    // 短暂休眠，避免毫秒级疯狂重放压垮数据库
                    parkOrInterrupt(batchFailSleep);
                    // 重置重连计数（因为这不是连接问题）
                    reconnectAttempts.set(0);
                }

            } catch (Exception e) {
                // 网络/连接异常：断开连接，定时重连
                if (connector != null) {
                    try {
                        connector.disconnect();
                    } catch (Exception ex) {
                        log.warn("断开异常连接时失败: {}", ex.getMessage());
                    }
                }
                reconnectWithRetry(reconnectSleep);
            }
        }
    }

    /**
     * 带重试机制的重连逻辑，使用指数退避策略
     *
     * @param reconnectSleep 基础重连间隔（毫秒）
     */
    private void reconnectWithRetry(long reconnectSleep) {
        if (parkOrInterrupt(reconnectSleep)) {
            running.set(false);
            return;
        }
        try {
            init();
            // 成功重连，重置计数器
            reconnectAttempts.set(0);
        } catch (Exception ex) {
            long currentAttempts = reconnectAttempts.incrementAndGet();

            if (currentAttempts >= MAX_RECONNECT_ATTEMPTS) {
                running.set(false);
                return;
            }
            // 指数退避策略
            long backoffSleep = Math.min(reconnectSleep * (long) Math.pow(2, currentAttempts), MAX_BACKOFF_SLEEP_MS);
            if (parkOrInterrupt(backoffSleep)) {
                running.set(false);
            }
        }
    }

    /**
     * 使用LockSupport阻塞当前线程指定毫秒数。
     * 阻塞期间可响应中断，中断后需调用方自行检查中断标志。
     *
     * @param millis 阻塞时间（毫秒）
     * @return true-current thread is interrupted; false-no
     */
    private boolean parkOrInterrupt(long millis) {
        if (millis > 0) {
            LockSupport.parkNanos(this, millis * 1_000_000L);
        }
        return Thread.currentThread().isInterrupted();
    }
}
