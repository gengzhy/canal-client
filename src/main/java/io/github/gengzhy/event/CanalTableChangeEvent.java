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

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 表级事件
 */
@Getter
public class CanalTableChangeEvent extends ApplicationEvent {
    private final String schemaName;
    private final String tableName;
    private final CanalEntry.EventType eventType;

    public CanalTableChangeEvent(Object source, String schemaName, String tableName, CanalEntry.EventType eventType) {
        super(source);
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.eventType = eventType;
    }
}