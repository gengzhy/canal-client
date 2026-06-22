# canal-spring-boot-starter

#### 组件简介

基于 [Canal ](https://github.com/alibaba/canal) 整合的 Starter，开箱即用。

> canal [kə'næl]，译意为水道/管道/沟渠，主要用途是基于 MySQL 数据库增量日志解析，提供增量数据订阅和消费
> 
> 早期阿里巴巴因为杭州和美国双机房部署，存在跨机房同步的业务需求，实现方式主要是基于业务 trigger 获取增量变更。从 2010 年开始，业务逐步尝试数据库日志解析获取增量变更进行同步，由此衍生出了大量的数据库增量订阅和消费业务。
>
> 基于日志增量订阅和消费的业务包括
>
>  - 数据库镜像
>  - 数据库实时备份
>  - 索引构建和实时维护(拆分异构索引、倒排索引等)
>  - 业务 cache 刷新
>  - 带业务逻辑的增量数据处理
> 
> 当前的 canal 支持源端 MySQL 版本包括 5.1.x , 5.5.x , 5.6.x , 5.7.x , 8.0.x

#### 工作原理
##### MySQL主备复制原理

 - MySQL master 将数据变更写入二进制日志( binary log, 其中记录叫做二进制日志事件binary log events，可以通过 show binlog events 进行查看)
 - MySQL slave 将 master 的 binary log events 拷贝到它的中继日志(relay log)
 - MySQL slave 重放 relay log 中事件，将数据变更反映它自己的数据

##### canal 工作原理
 - canal 模拟 MySQL slave 的交互协议，伪装自己为 MySQL slave ，向 MySQL master 发送dump 协议
 - MySQL master 收到 dump 请求，开始推送 binary log 给 slave (即 canal )
 - canal 解析 binary log 对象(原始为 byte 流)

#### 使用说明

##### 1 Spring Boot 项目添加 Maven 依赖

``` xml
<dependency>
	<groupId>io.github.gengzhy</groupId>
	<artifactId>canal-spring-boot-starter</artifactId>
	<version>1.0.0</version>
</dependency>
```

##### 2 使用示例

###### 2.1 根据实际业务需求选择不同的客户端模式

在`application.yml`文件中增加如下配置（单机模式为例）

```yaml
spring:
  canal:
    enabled: true
    mode: simple
    simple:
      destination: example
      host: 127.0.0.1
      port: 11111
      so-timeout: PT60S
      idle-timeout: PT1H
      batch-size: 500
      poll-sleep: 100
      batch-fail-sleep: PT5S
      reconnect-sleep: PT3S
      batch-transaction-enabled: false
```

###### 2.2 创建Java对象 Bean [CanalClientListener.java](src/test/java/io/github/gengzhy/CanalClientListener.java)，
对以下三个事件进行监听，从而进行业务逻辑处理：
[CanalRawBinlogEvent.java](src/main/java/io/github/gengzhy/event/CanalRawBinlogEvent.java)、
[CanalTableChangeEvent.java](src/main/java/io/github/gengzhy/event/CanalTableChangeEvent.java)、
[CanalRowDataEvent.java](src/main/java/io/github/gengzhy/event/CanalRowDataEvent.java) 

```java
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
```
