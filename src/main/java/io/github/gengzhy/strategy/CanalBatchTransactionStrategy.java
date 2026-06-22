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

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import io.github.gengzhy.event.CanalRawBinlogEvent;
import io.github.gengzhy.event.CanalRowDataEvent;
import io.github.gengzhy.event.CanalTableChangeEvent;
import io.github.gengzhy.parser.MetadataParser;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * 批次事务策略抽象：完全解耦事务与事件分发逻辑，可插拔切换事务类型
 */
public interface CanalBatchTransactionStrategy {

    /**
     * 开启批次事务，返回事务上下文，供后续提交/回滚
     */
    Object openTransaction();

    /**
     * 执行事件发布全流程（原始事件/表事件/行事件）
     */
    default void publishAllEvent(Message message, ApplicationEventPublisher eventPublisher) throws Exception {
        // 事件发布逻辑不变，仅无事务包裹
        CanalRawBinlogEvent rawEvent = new CanalRawBinlogEvent(this, message);
        eventPublisher.publishEvent(rawEvent);

        List<CanalEntry.Entry> entryList = message.getEntries();
        for (CanalEntry.Entry entry : entryList) {
            if (!CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())) {
                continue;
            }
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            CanalTableChangeEvent tableEvent =  metadataParser().parseTableEvent(entry);
            eventPublisher.publishEvent(tableEvent);
            List<CanalRowDataEvent> rowEvents =  metadataParser().parseRowData(entry, rowChange);
            rowEvents.forEach(eventPublisher::publishEvent);
        }
    }

    /**
     * 拿到元数据解析器实例
     */
    MetadataParser metadataParser();

    /**
     * 事务正常提交
     */
    void commit(Object transactionContext);

    /**
     * 事务异常回滚
     */
    void rollback(Object transactionContext);
}