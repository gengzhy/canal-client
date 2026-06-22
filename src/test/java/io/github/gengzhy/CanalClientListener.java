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

package io.github.gengzhy;

import io.github.gengzhy.event.CanalRawBinlogEvent;
import io.github.gengzhy.event.CanalRowDataEvent;
import io.github.gengzhy.event.CanalTableChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 测试Canal事件监听器
 *
 * @author gengzhy
 * @version 1.0.0
 * @since 2026-06-17 14:21
 */
@Slf4j
@Component
public class CanalClientListener {

    @EventListener(CanalRawBinlogEvent.class)
    void onListenCanalRawBinlogEvent(CanalRawBinlogEvent event) {
        log.info("CanalRawBinlogEvent received: {}", event);
    }

    @EventListener(CanalTableChangeEvent.class)
    void onListenCanalTableChangeEvent(CanalTableChangeEvent event) {
        log.info("CanalTableChangeEvent received: {}", event);
    }

    @EventListener(CanalRowDataEvent.class)
    void onListenCanalRowDataEvent(CanalRowDataEvent event) {
        log.info("onListenCanalRowDataEvent received: {}", event);
    }
}
