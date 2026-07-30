package com.awe.foundation.module.ddl.config;

import com.awe.foundation.module.ddl.config.properties.DdlMonitorProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MySQL DDL 监听配置
 *
 * @author Awe
 * @since 2026/7/30
 */
@Configuration
@EnableConfigurationProperties(DdlMonitorProperties.class)
public class DdlMonitorConfig {
}
