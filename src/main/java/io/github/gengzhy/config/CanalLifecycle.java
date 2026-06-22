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

import io.github.gengzhy.client.CanalClient;
import org.springframework.context.SmartLifecycle;

public class CanalLifecycle implements SmartLifecycle {

    private final CanalClient client;
    private volatile boolean started = false;

    public CanalLifecycle(CanalClient client) {
        this.client = client;
    }

    @Override
    public void start() {
        client.start();
        started = true;
    }

    @Override
    public void stop() {
        client.stop();
        started = false;
    }

    @Override
    public boolean isRunning() {
        return started && client.isRunning();
    }

    // 启动执行优先级，低于数据源，高于业务Bean
    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}