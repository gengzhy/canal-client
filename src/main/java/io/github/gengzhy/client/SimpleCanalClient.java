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

import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import io.github.gengzhy.config.CanalProperties;
import io.github.gengzhy.dispatch.CanalEventDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.net.InetSocketAddress;

/**
 * Client 客户端默认实现，单机版
 *
 * @author gengzhy
 * @version 1.0.0
 * @since 2026-06-17 10:51
 */
@Slf4j
public class SimpleCanalClient extends AbstractCanalClient {

    public SimpleCanalClient(CanalProperties properties, CanalEventDispatcher eventDispatcher) {
        super(properties, eventDispatcher);
    }

    /**
     * 初始化Canal客户端
     */
    protected final void init() {
        CanalProperties.Simple simple = getProperties().getSimple();
        connector = new SimpleCanalConnector(
                new InetSocketAddress(simple.getHost(), simple.getPort()),
                StringUtils.trimToEmpty(simple.getUsername()),
                StringUtils.trimToEmpty(simple.getPassword()),
                simple.getDestination(),
                simple.getSoTimeout().toMillisPart(),
                simple.getIdleTimeout().toMillisPart()
        );
        try {
            connector.connect();
            connector.subscribe();
            connector.rollback();
            log.info("[{}] Canal连接初始化完成", simple.getDestination());
        } catch (Exception e) {
            log.warn("Canal初始化连接失败，将在消费循环中自动重连: {}", e.getMessage());
        }
    }

    /**
     * 每次拉取服务端数据量
     */
    @Override
    protected int batchSize() {
        return getProperties().getSimple().getBatchSize();
    }
}
