package com.awe.qd.module.ddl.listener;

import com.awe.qd.module.ddl.config.properties.DdlMonitorProperties;
import com.awe.qd.module.ddl.domain.DdlChangeMessage;
import com.awe.qd.module.ddl.domain.JdbcEndpoint;
import com.awe.qd.module.ddl.notify.DingTalkWebhookNotifier;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;

import java.net.URI;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 MySQL Binlog 监听表结构 DDL 变化并通知
 *
 * @author Awe
 * @since 2026/7/30
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "ddl-monitor", name = "enabled", havingValue = "true")
public class MysqlDdlBinlogListener {

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile(
            "^jdbc:(?:p6spy:)?mysql://([^/?#:]+)(?::(\\d+))?/([^?]+)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DDL_PATTERN = Pattern.compile(
            "(CREATE|ALTER|DROP|RENAME|TRUNCATE)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SQL_BLOCK_COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    private static final Pattern SQL_LINE_COMMENT_PATTERN = Pattern.compile("^\\s*--.*?$", Pattern.MULTILINE);

    private static final Pattern DDL_META_PATTERN = Pattern.compile(
            "(?i)^(CREATE|ALTER|DROP|RENAME|TRUNCATE)\\s+(?:OR\\s+REPLACE\\s+)?"
                    + "(TEMPORARY\\s+)?(TABLE|INDEX|VIEW|UNIQUE\\s+INDEX|FULLTEXT\\s+INDEX|SPATIAL\\s+INDEX)?"
                    + "(?:\\s+IF\\s+(?:NOT\\s+)?EXISTS)?\\s*"
                    + "(?:`?([\\w]+)`?\\.)?`?([\\w]+)`?");

    @Resource
    private DdlMonitorProperties ddlMonitorProperties;

    @Resource
    private DataSourceProperties dataSourceProperties;

    @Resource
    private DingTalkWebhookNotifier dingTalkWebhookNotifier;

    private BinaryLogClient binaryLogClient;

    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * 应用启动完成后开启 Binlog DDL 监听
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startListen() {
        if (!ddlMonitorProperties.isEnabled()) {
            log.info("DDL 监听未开启，跳过启动");
            return;
        }
        if (!started.compareAndSet(false, true)) {
            return;
        }

        // 1. 解析数据源连接信息与监听库列表
        JdbcEndpoint jdbcEndpoint = parseJdbcEndpoint(dataSourceProperties.getUrl());
        String username = dataSourceProperties.getUsername();
        String password = dataSourceProperties.getPassword();
        Set<String> listenDatabases = resolveListenDatabases(jdbcEndpoint.getDatabase());

        // 2. 构建 Binlog 客户端（跳过行变更反序列化，只关心 DDL 的 QueryEvent）
        binaryLogClient = new BinaryLogClient(jdbcEndpoint.getHost(), jdbcEndpoint.getPort(), username, password);
        binaryLogClient.setServerId(ddlMonitorProperties.getServerId());
        binaryLogClient.setConnectTimeout(ddlMonitorProperties.getConnectTimeout());
        binaryLogClient.setKeepAliveInterval(ddlMonitorProperties.getKeepaliveInterval());
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setEventDataDeserializer(EventType.TABLE_MAP, new NullEventDataDeserializer());
        eventDeserializer.setEventDataDeserializer(EventType.WRITE_ROWS, new NullEventDataDeserializer());
        eventDeserializer.setEventDataDeserializer(EventType.UPDATE_ROWS, new NullEventDataDeserializer());
        eventDeserializer.setEventDataDeserializer(EventType.DELETE_ROWS, new NullEventDataDeserializer());
        eventDeserializer.setEventDataDeserializer(EventType.EXT_WRITE_ROWS, new NullEventDataDeserializer());
        eventDeserializer.setEventDataDeserializer(EventType.EXT_UPDATE_ROWS, new NullEventDataDeserializer());
        eventDeserializer.setEventDataDeserializer(EventType.EXT_DELETE_ROWS, new NullEventDataDeserializer());
        binaryLogClient.setEventDeserializer(eventDeserializer);
        binaryLogClient.registerEventListener(event -> {
            EventData eventData = event.getData();
            if (!(eventData instanceof QueryEventData queryEventData)) {
                return;
            }
            String sql = queryEventData.getSql();
            if (StringUtils.isBlank(sql) || !isTableDdl(sql)) {
                return;
            }
            String eventDatabase = queryEventData.getDatabase();
            String ddlSql = sql.trim();
            if (CollUtil.isNotEmpty(listenDatabases)
                    && StringUtils.isNotBlank(eventDatabase)
                    && !containsIgnoreCase(listenDatabases, eventDatabase)) {
                log.debug("忽略非目标库 DDL，listenDatabases={}, eventDatabase={}, ddl={}",
                        listenDatabases, eventDatabase, ddlSql);
                return;
            }

            // 3. 解析并推送 DDL 变化
            long timestamp = 0L;
            if (event.getHeader() instanceof EventHeaderV4 eventHeaderV4) {
                timestamp = eventHeaderV4.getTimestamp();
            }
            String databaseName = StringUtils.defaultIfBlank(eventDatabase,
                    CollUtil.getFirst(listenDatabases));
            DdlChangeMessage changeMessage = buildDdlChangeMessage(ddlSql, databaseName, timestamp, jdbcEndpoint);
            log.info("检测到表结构 DDL 变化，database={}, changeType={}, objectName={}, timestamp={}, ddl={}",
                    changeMessage.getDatabase(), changeMessage.getChangeType(), changeMessage.getObjectName(),
                    timestamp, ddlSql);
            dingTalkWebhookNotifier.notifyDdlChange(changeMessage);
        });
        binaryLogClient.registerLifecycleListener(new BinaryLogClient.AbstractLifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                log.info("MySQL Binlog DDL 监听已连接，host={}:{}, databases={}, serverId={}",
                        jdbcEndpoint.getHost(), jdbcEndpoint.getPort(), listenDatabases, ddlMonitorProperties.getServerId());
            }

            @Override
            public void onDisconnect(BinaryLogClient client) {
                log.warn("MySQL Binlog DDL 监听已断开，host={}:{}", jdbcEndpoint.getHost(), jdbcEndpoint.getPort());
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
                log.error("MySQL Binlog DDL 监听通信异常，host={}:{}", jdbcEndpoint.getHost(), jdbcEndpoint.getPort(), ex);
            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
                log.error("MySQL Binlog DDL 事件反序列化失败", ex);
            }
        });

        // 4. 后台线程持续消费 Binlog
        Thread listenThread = new Thread(() -> {
            try {
                binaryLogClient.connect();
            } catch (Exception e) {
                started.set(false);
                log.error("启动 MySQL Binlog DDL 监听失败，请确认已开启 binlog，且账号具备 REPLICATION SLAVE/CLIENT 权限", e);
            }
        }, "mysql-ddl-binlog-listener");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    /**
     * 应用关闭时断开 Binlog 连接
     */
    @PreDestroy
    public void stopListen() {
        if (Objects.isNull(binaryLogClient)) {
            return;
        }
        try {
            binaryLogClient.disconnect();
            log.info("MySQL Binlog DDL 监听已停止");
        } catch (Exception e) {
            log.warn("停止 MySQL Binlog DDL 监听异常", e);
        } finally {
            started.set(false);
        }
    }

    /**
     * 构建 DDL 变更通知消息
     *
     * @param ddlSql       DDL 原文
     * @param databaseName 数据库名
     * @param timestamp    事件时间戳
     * @param jdbcEndpoint 连接端点
     * @return 通知消息
     */
    private DdlChangeMessage buildDdlChangeMessage(String ddlSql, String databaseName,
                                                   long timestamp, JdbcEndpoint jdbcEndpoint) {
        String normalizedSql = SQL_BLOCK_COMMENT_PATTERN.matcher(ddlSql).replaceAll(" ");
        normalizedSql = SQL_LINE_COMMENT_PATTERN.matcher(normalizedSql).replaceAll(" ");
        normalizedSql = normalizedSql.trim();

        String changeType = "DDL";
        String objectType = "-";
        String objectName = "-";
        Matcher metaMatcher = DDL_META_PATTERN.matcher(normalizedSql);
        if (metaMatcher.find()) {
            changeType = StringUtils.upperCase(metaMatcher.group(1), Locale.ROOT);
            String parsedObjectType = metaMatcher.group(3);
            if (StringUtils.isNotBlank(parsedObjectType)) {
                objectType = StringUtils.upperCase(parsedObjectType.replaceAll("\\s+", " "), Locale.ROOT);
            } else if (StringUtils.equalsIgnoreCase(changeType, "TRUNCATE")
                    || StringUtils.equalsIgnoreCase(changeType, "RENAME")) {
                objectType = "TABLE";
            }
            if (StringUtils.isNotBlank(metaMatcher.group(5))) {
                objectName = metaMatcher.group(5);
            }
        }

        return DdlChangeMessage.builder()
                .database(databaseName)
                .changeType(changeType)
                .objectType(objectType)
                .objectName(objectName)
                .ddlSql(ddlSql)
                .eventTime(timestamp)
                .host(jdbcEndpoint.getHost())
                .port(jdbcEndpoint.getPort())
                .build();
    }

    /**
     * 解析监听库列表：配置优先，未配置时回退到数据源库名
     *
     * @param defaultDatabase 数据源默认库名
     * @return 小写库名集合
     */
    private Set<String> resolveListenDatabases(String defaultDatabase) {
        Set<String> databases = new HashSet<>();
        if (CollUtil.isNotEmpty(ddlMonitorProperties.getDatabases())) {
            for (String database : ddlMonitorProperties.getDatabases()) {
                if (StringUtils.isNotBlank(database)) {
                    databases.add(database.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        if (CollUtil.isEmpty(databases) && StringUtils.isNotBlank(defaultDatabase)) {
            databases.add(defaultDatabase.toLowerCase(Locale.ROOT));
        }
        return databases;
    }

    /**
     * 判断事件库是否在监听列表中（忽略大小写）
     *
     * @param listenDatabases 监听库集合（小写）
     * @param eventDatabase   事件库名
     * @return true-命中
     */
    private boolean containsIgnoreCase(Set<String> listenDatabases, String eventDatabase) {
        return listenDatabases.contains(eventDatabase.toLowerCase(Locale.ROOT));
    }

    /**
     * 判断 SQL 是否为表结构相关 DDL
     *
     * @param sql SQL 语句
     * @return true-表结构 DDL
     */
    private boolean isTableDdl(String sql) {
        // DataGrip 等客户端会在 SQL 前附加 /* ApplicationName=... */ 注释，需先剥离
        String normalizedSql = SQL_BLOCK_COMMENT_PATTERN.matcher(sql).replaceAll(" ");
        normalizedSql = SQL_LINE_COMMENT_PATTERN.matcher(normalizedSql).replaceAll(" ");
        normalizedSql = normalizedSql.trim();
        if (StringUtils.isBlank(normalizedSql)
                || StringUtils.equalsAnyIgnoreCase(normalizedSql, "BEGIN", "COMMIT", "ROLLBACK")) {
            return false;
        }
        Matcher matcher = DDL_PATTERN.matcher(normalizedSql);
        if (!matcher.find()) {
            return false;
        }
        // 确保 DDL 关键字出现在语句起始位置（忽略前导空白与已剥离注释）
        if (matcher.start() != 0) {
            return false;
        }
        String keyword = matcher.group(1).toUpperCase(Locale.ROOT);
        String upperSql = normalizedSql.toUpperCase(Locale.ROOT);
        // 仅关注表结构相关 DDL，过滤用户/权限等语句
        return switch (keyword) {
            case "CREATE", "ALTER", "DROP" -> upperSql.contains(" TABLE")
                    || upperSql.contains(" INDEX")
                    || upperSql.contains(" VIEW");
            case "RENAME" -> upperSql.contains("TABLE");
            case "TRUNCATE" -> true;
            default -> false;
        };
    }

    /**
     * 解析 JDBC URL 中的主机、端口、库名
     *
     * @param jdbcUrl 数据源 URL
     * @return 连接端点
     */
    private JdbcEndpoint parseJdbcEndpoint(String jdbcUrl) {
        if (StringUtils.isBlank(jdbcUrl)) {
            throw new IllegalArgumentException("数据源 URL 为空，无法启动 DDL 监听");
        }
        Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);
        if (matcher.find()) {
            String host = matcher.group(1);
            int port = StringUtils.isNotBlank(matcher.group(2)) ? Integer.parseInt(matcher.group(2)) : 3306;
            String database = matcher.group(3);
            return JdbcEndpoint.builder().host(host).port(port).database(database).build();
        }
        // 兼容少见写法：退化为 URI 解析
        String normalizedUrl = jdbcUrl.replaceFirst("(?i)^jdbc:(?:p6spy:)?mysql:", "mysql:");
        URI uri = URI.create(normalizedUrl);
        String database = StringUtils.removeStart(uri.getPath(), "/");
        int port = uri.getPort() > 0 ? uri.getPort() : 3306;
        return JdbcEndpoint.builder().host(uri.getHost()).port(port).database(database).build();
    }

}
