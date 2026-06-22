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

/**
 * 客户端模式
 * <p>
 * 支持：simple, cluster, kafka, pulsarmq, rabbitmq, rocketmq
 *
 * @author gengzhy
 * @version 1.0.0
 * @since 2026-06-18 09:04
 */
public enum ClientMode {
    /**
     * 单机模式
     */
    simple,
    /**
     * 集群模式，搭配 zookeeper 使用
     */
    cluster,
    /**
     * 消息队列 kafka
     */
    kafka,
    /**
     * 消息队列 pulsar MQ
     */
    pulsarmq,
    /**
     * 消息队列 rabbit MQ
     */
    rabbitmq,
    /**
     * 消息队列 rocket MQ
     */
    rocketmq
}
