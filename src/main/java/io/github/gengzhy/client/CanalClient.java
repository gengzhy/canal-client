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

package io.github.gengzhy.client;

/**
 * Canal客户端基类
 *
 * @author gengzhy
 * @version 1.0.0
 * @since 2026-06-17 10:47
 */
public interface CanalClient {

    /**
     * 开启Canal客户端执行逻辑
     */
    void start();

    /**
     * 停止Canal客户端
     */
    void stop();

    /**
     * 当前客户端是否正在运行中
     *
     * @return true-运行中/false-已停止
     */
    boolean isRunning();
}
