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

package io.github.gengzhy.parser;

import com.alibaba.otter.canal.protocol.CanalEntry;
import io.github.gengzhy.event.CanalRowDataEvent;
import io.github.gengzhy.event.CanalTableChangeEvent;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认元数据解析器基类
 */
@Slf4j
public class DefaultMetadataParser implements MetadataParser {

    @Override
    public CanalTableChangeEvent parseTableEvent(CanalEntry.Entry entry) {
        String schema = entry.getHeader().getSchemaName();
        String table = entry.getHeader().getTableName();
        CanalEntry.EventType eventType = entry.getHeader().getEventType();
        return new CanalTableChangeEvent(this, schema, table, eventType);
    }

    @Override
    public List<CanalRowDataEvent> parseRowData(CanalEntry.Entry entry, CanalEntry.RowChange rowChange) {
        List<CanalRowDataEvent> result = new ArrayList<>();
        if (rowChange == null) {
            log.warn("行变更RowChange数据为空，跳过本条解析");
            return result;
        }
        String schema = entry.getHeader().getSchemaName();
        String table = entry.getHeader().getTableName();
        String operateType = rowChange.getEventType().name();

        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
            Map<String, Object> oldMap = new HashMap<>(16);
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                oldMap.put(column.getName(), column.getValue());
            }

            Map<String, Object> newMap = new HashMap<>(16);
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                newMap.put(column.getName(), column.getValue());
            }
            CanalRowDataEvent rowEvent = new CanalRowDataEvent(this, schema, table, operateType, oldMap, newMap);
            result.add(rowEvent);
        }
        return result;
    }
}