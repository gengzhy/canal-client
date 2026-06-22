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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 本地 JDBC 全局事务
 */
@Slf4j
public record LocalBatchTransactionStrategy(PlatformTransactionManager transactionManager,
                                            MetadataParser metadataParser) implements CanalBatchTransactionStrategy {

    @Override
    public Object openTransaction() {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        txDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txDef.setName("canal-batch-global-tx");
        return transactionManager.getTransaction(txDef);
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
        TransactionStatus txStatus = (TransactionStatus) transactionContext;
        transactionManager.commit(txStatus);
        log.debug("批次全局本地事务提交成功");
    }

    @Override
    public void rollback(Object transactionContext) {
        TransactionStatus txStatus = (TransactionStatus) transactionContext;
        transactionManager.rollback(txStatus);
        log.error("批次全局本地事务执行回滚");
    }
}