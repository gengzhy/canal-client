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

import java.util.List;

/**
 * 通用元数据解析器基类
 */
public interface MetadataParser {
    default CanalTableChangeEvent parseTableEvent(CanalEntry.Entry entry) {
        return null;
    }

    default List<CanalRowDataEvent> parseRowData(CanalEntry.Entry entry, CanalEntry.RowChange rowChange) {
        return null;
    }
}