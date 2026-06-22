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

package io.github.gengzhy.strategy;

import io.github.gengzhy.parser.MetadataParser;
import lombok.extern.slf4j.Slf4j;

/**
 * 空事务实现
 */
@Slf4j
public record NoneBatchTransactionStrategy(MetadataParser metadataParser) implements CanalBatchTransactionStrategy {

    @Override
    public Object openTransaction() {
        // 无事务，返回空上下文标识
        return null;
    }

    /**
     * 拿到元数据解析器实例
     */
    @Override
    public MetadataParser metadataParser() {
        return this.metadataParser;
    }

    @Override
    public void commit(Object transactionContext) {
        // 空实现，无需提交事务
    }

    @Override
    public void rollback(Object transactionContext) {
        // 空实现，无事务可回滚
        log.warn("当前已关闭批次全局事务，业务异常无法自动批量回滚数据");
    }
}