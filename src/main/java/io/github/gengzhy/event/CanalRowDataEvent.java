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

package io.github.gengzhy.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.Collections;
import java.util.Map;

/**
 * 行数据业务事件
 */
@Getter
public class CanalRowDataEvent extends ApplicationEvent {
    private final String schemaName;
    private final String tableName;
    private final String operateType;
    private final Map<String, Object> oldFieldMap;
    private final Map<String, Object> newFieldMap;

    public CanalRowDataEvent(Object source,
                             String schemaName,
                             String tableName,
                             String operateType,
                             Map<String, Object> oldFieldMap,
                             Map<String, Object> newFieldMap) {
        super(source);
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.operateType = operateType;
        // 包装不可变集合，监听方无法篡改原始数据
        this.oldFieldMap = Collections.unmodifiableMap(oldFieldMap);
        this.newFieldMap = Collections.unmodifiableMap(newFieldMap);
    }
}