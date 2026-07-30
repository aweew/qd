package com.awe.foundation.module.ddl.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL DDL 监听配置属性
 *
 * @author Awe
 * @since 2026/7/30
 */
@Data
@ConfigurationProperties(prefix = "ddl-monitor")
public class DdlMonitorProperties {

    /**
     * 是否开启 DDL 监听
     */
    private boolean enabled = false;

    /**
     * Binlog 客户端 serverId，需保证在同一 MySQL 实例下唯一
     */
    private long serverId = 5642L;

    /**
     * 仅监听指定库的 DDL，为空则自动取 spring.datasource.url 中的库名
     */
    private List<String> databases = new ArrayList<>();

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 心跳间隔（毫秒），0 表示关闭
     */
    private long keepaliveInterval = 15000L;

}
