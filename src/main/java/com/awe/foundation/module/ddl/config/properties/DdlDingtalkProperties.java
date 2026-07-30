package com.awe.foundation.module.ddl.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DDL 变更钉钉机器人通知配置
 *
 * @author Awe
 * @since 2026/7/30
 */
@Data
@ConfigurationProperties(prefix = "ddl-monitor.dingtalk")
public class DdlDingtalkProperties {

    /**
     * 是否开启钉钉通知
     */
    private boolean enabled = false;

    /**
     * 钉钉自定义机器人 Webhook 地址（含 access_token）
     */
    private String webhookUrl;

    /**
     * 钉钉机器人加签密钥（SEC 开头）
     */
    private String secret;

}
