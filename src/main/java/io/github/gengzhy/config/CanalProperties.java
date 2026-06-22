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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.canal")
public class CanalProperties {

    /**
     * Canal消费总开关 默认true
     */
    private boolean enabled = true;

    /**
     * 无数据休眠毫秒，降低CPU空轮询。默认1秒
     */
    private Duration pollSleep = Duration.ofSeconds(1);
    /**
     * 网络断开重连间隔。默认3秒
     */
    private Duration reconnectSleep = Duration.ofSeconds(3);
    /**
     * 批次处理失败后休眠，防止疯狂重放压库。默认5秒
     */
    private Duration batchFailSleep = Duration.ofSeconds(5);
    /**
     * 全局事务开关。默认true
     */
    private boolean txEnabled = true;

    /**
     * 客户端模式，默认 simple
     * <p>
     * 支持：simple, cluster, kafka, pulsarmq, rabbitmq, rocketmq
     */
    private ClientMode mode = ClientMode.simple;

    /**
     * 单机模式配置
     */
    private Simple simple = new Simple();

    /**
     * 集群模式配置
     */
    private Cluster cluster;


    @Getter
    @Setter
    public static class Simple {
        /**
         * canal服务主机host
         */
        private String host = "127.0.0.1";

        /**
         * Canal服务主机port，默认 11111
         */
        private int port = 11111;

        /**
         * canal实例 destination
         */
        private String destination = "example";
        /**
         * Canal Server 账号
         */
        private String username = "";
        /**
         * Canal Server 密码
         */
        private String password = "";
        /**
         * Socket 连接超时时间。官方默认 60秒
         */
        private Duration soTimeout = Duration.ofSeconds(60);
        /**
         * client和server之间的Socket空闲链接超时的时间，官方默认为1小时
         */
        private Duration idleTimeout = Duration.ofHours(1);

        /**
         * 每次拉取服务端数据量。官方默认1000条
         */
        private int batchSize = 1000;

    }

    @Getter
    @Setter
    public static class Cluster {

        /**
         * Canal Server 地址
         */
        private String addresses;
        /**
         * Canal Zookeeper 地址。如果设置了该属性，则忽略addresses属性。
         */
        private String zkServers;
        /**
         * canal实例 destination
         */
        private String destination;
        /**
         * Canal Server 账号
         */
        private String username;
        /**
         * Canal Server 密码
         */
        private String password;
        /**
         * Socket 连接超时时间。官方默认 60秒
         */
        private Duration soTimeout = Duration.ofSeconds(60);
        /**
         * client和server之间的Socket空闲链接超时的时间，官方默认为1小时
         */
        private Duration idleTimeout = Duration.ofHours(1);
        /**
         * 每次拉取服务端数据量。官方默认1000条
         */
        private int batchSize = 1000;
    }
}