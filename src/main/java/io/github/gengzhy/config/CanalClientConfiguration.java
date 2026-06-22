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

package io.github.gengzhy.config;

import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import io.github.gengzhy.client.CanalClient;
import io.github.gengzhy.client.ClusterCanalClient;
import io.github.gengzhy.client.SimpleCanalClient;
import io.github.gengzhy.dispatch.CanalEventDispatcher;
import io.github.gengzhy.dispatch.DefaultCanalEventDispatcher;
import io.github.gengzhy.parser.DefaultMetadataParser;
import io.github.gengzhy.parser.MetadataParser;
import io.github.gengzhy.strategy.CanalBatchTransactionStrategy;
import io.github.gengzhy.strategy.NoneBatchTransactionStrategy;
import io.github.gengzhy.strategy.LocalBatchTransactionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Canal客户端配置类
 *
 * @author gengzhy
 * @version 1.0.0
 * @since 2026-06-17 11:12
 */
@Configuration
@ConditionalOnClass(com.alibaba.otter.canal.client.CanalConnector.class)
@EnableConfigurationProperties(CanalProperties.class)
@ConditionalOnProperty(prefix = "spring.canal", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CanalClientConfiguration {

    @Configuration
    @ConditionalOnClass(SimpleCanalConnector.class)
    static class SimpleClientConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "spring.canal", name = "mode", havingValue = "simple", matchIfMissing = true)
        CanalClient canalClient(CanalProperties canalProperties, CanalEventDispatcher eventDispatcher) {
            return new SimpleCanalClient(canalProperties, eventDispatcher);
        }
    }

    @Configuration
    @ConditionalOnClass(ClusterCanalConnector.class)
    @ConditionalOnProperty(prefix = "spring.canal", name = "mode", havingValue = "cluster")
    static class classClusterClientConfiguration {
        @Bean
        @ConditionalOnMissingBean
        CanalClient canalClient(CanalProperties canalProperties, CanalEventDispatcher eventDispatcher) {
            return new ClusterCanalClient(canalProperties, eventDispatcher);
        }
    }

    /**
     * 注册元数据解析器：用户自定义MetadataParser自动覆盖默认实现
     */
    @Bean
    @ConditionalOnMissingBean
    public MetadataParser metadataParser() {
        return new DefaultMetadataParser();
    }

    /**
     * 注册事件分发器
     */
    @Bean
    @ConditionalOnMissingBean
    public CanalEventDispatcher canalEventDispatcher(ApplicationEventPublisher eventPublisher,
                                                     CanalBatchTransactionStrategy batchTransactionStrategy) {
        return new DefaultCanalEventDispatcher(eventPublisher, batchTransactionStrategy);
    }

    /**
     * 注册事务策略（存在 PlatformTransactionManager 时使用本地事务）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(PlatformTransactionManager.class)
    public CanalBatchTransactionStrategy batchTransactionStrategyWithTx(PlatformTransactionManager transactionManager,
                                                                        MetadataParser metadataParser,
                                                                        CanalProperties canalProperties) {
        if (canalProperties.isTxEnabled()) {
            return new LocalBatchTransactionStrategy(transactionManager, metadataParser);
        } else {
            return new NoneBatchTransactionStrategy(metadataParser);
        }
    }

    /**
     * 注册事务策略（无 PlatformTransactionManager 时降级为空事务）
     */
    @Bean
    @ConditionalOnMissingBean
    public CanalBatchTransactionStrategy batchTransactionStrategyWithoutTx(MetadataParser metadataParser) {
        return new NoneBatchTransactionStrategy(metadataParser);
    }

    /**
     * 生命周期
     */
    @Bean
    @ConditionalOnMissingBean
    public CanalLifecycle canalLifecycle(CanalClient client) {
        return new CanalLifecycle(client);
    }
}
