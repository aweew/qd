package com.awe.foundation.module.ddl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DDL 变更通知消息
 *
 * @author Awe
 * @since 2026/7/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DdlChangeMessage {

    /**
     * 数据库名
     */
    private String database;

    /**
     * 变更类型，如 ALTER / CREATE / DROP
     */
    private String changeType;

    /**
     * 变更对象类型，如 TABLE / INDEX / VIEW
     */
    private String objectType;

    /**
     * 变更对象名称
     */
    private String objectName;

    /**
     * DDL 原文
     */
    private String ddlSql;

    /**
     * 事件时间戳（毫秒）
     */
    private Long eventTime;

    /**
     * MySQL 主机
     */
    private String host;

    /**
     * MySQL 端口
     */
    private Integer port;

}
