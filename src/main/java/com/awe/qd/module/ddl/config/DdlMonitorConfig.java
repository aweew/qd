package com.awe.qd.module.ddl.config;

import com.awe.qd.module.ddl.config.properties.DdlDingtalkProperties;
import com.awe.qd.module.ddl.config.properties.DdlMonitorProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MySQL DDL 监听配置
 *
 * @author Awe
 * @since 2026/7/30
 */
@Configuration
@EnableConfigurationProperties({DdlMonitorProperties.class, DdlDingtalkProperties.class})
public class DdlMonitorConfig {
}
