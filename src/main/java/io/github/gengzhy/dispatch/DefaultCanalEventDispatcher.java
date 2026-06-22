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

package io.github.gengzhy.dispatch;

import com.alibaba.otter.canal.protocol.Message;
import io.github.gengzhy.strategy.CanalBatchTransactionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 *
 * 默认消息分发器
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCanalEventDispatcher implements CanalEventDispatcher {

    private final ApplicationEventPublisher eventPublisher;
    private final CanalBatchTransactionStrategy batchTransactionStrategy;

    @Override
    public void dispatch(Message message) throws Exception {
        // 1. 由事务策略创建事务上下文
        Object txContext = batchTransactionStrategy.openTransaction();
        try {
            // 2. 委托策略执行全量事件发布
            batchTransactionStrategy.publishAllEvent(message, eventPublisher);
            // 3. 正常提交事务
            batchTransactionStrategy.commit(txContext);
        } catch (Exception e) {
            // 4. 异常统一回滚事务，由策略实现区分本地/空事务
            batchTransactionStrategy.rollback(txContext);
            log.error("批次事件处理发生异常", e);
            throw e;
        }
    }
}