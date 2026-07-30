package com.awe.foundation.module.ddl.notify;

import cn.hutool.core.date.DateUtil;
import com.awe.foundation.module.ddl.domain.DdlChangeMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * DDL 变更钉钉 Markdown 消息构建
 *
 * @author Awe
 * @since 2026/7/30
 */
public final class DdlChangeMarkdownBuilder {

    private static final Pattern SQL_BLOCK_COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    private static final Pattern SQL_LINE_COMMENT_PATTERN = Pattern.compile("^\\s*--.*?$", Pattern.MULTILINE);

    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * DDL 正文最大展示长度，超出截断避免建表语句撑爆消息
     */
    private static final int DDL_DISPLAY_MAX_LENGTH = 280;

    private DdlChangeMarkdownBuilder() {
    }

    /**
     * 构建钉钉通知标题（会话列表摘要）
     *
     * @param message DDL 变更消息
     * @return 标题
     */
    public static String buildTitle(DdlChangeMessage message) {
        String changeType = StringUtils.defaultIfBlank(message.getChangeType(), "DDL");
        String database = StringUtils.defaultIfBlank(message.getDatabase(), "unknown");
        String objectName = StringUtils.defaultIfBlank(message.getObjectName(), "-");
        return changeType + " | " + database + "." + objectName;
    }

    /**
     * 构建钉钉 Markdown 正文（对齐 Jenkins 通知风格：标题 + 圆点列表 + 关键值着色）
     *
     * @param message DDL 变更消息
     * @return Markdown 文本
     */
    public static String buildText(DdlChangeMessage message) {
        String changeType = StringUtils.defaultIfBlank(message.getChangeType(), "DDL");
        String objectName = StringUtils.defaultIfBlank(message.getObjectName(), "-");
        String database = StringUtils.defaultIfBlank(message.getDatabase(), "-");
        String host = StringUtils.defaultIfBlank(message.getHost(), "-");
        String port = Objects.nonNull(message.getPort()) ? String.valueOf(message.getPort()) : "-";
        String eventTime = formatEventTime(message.getEventTime());
        String ddlSql = cleanDdlSql(message.getDdlSql());

        StringBuilder markdown = new StringBuilder();
        markdown.append("**MySQL DDL 变更通知**").append("\n\n");
        markdown.append("- **变更类型**: ").append(colorText(changeType, resolveChangeTypeColor(changeType))).append("\n");
        markdown.append("- **数据库**: ").append(colorText(database, "#00B42A")).append("\n");
        markdown.append("- **表名称**: ").append(colorText(objectName, "#1677FF")).append("\n");
        markdown.append("- **实例地址**: ").append(host).append(":").append(port).append("\n");
        markdown.append("- **变更时间**: ").append(eventTime).append("\n");
        markdown.append("- **DDL 语句**: ").append("\n");
        markdown.append(formatDdlDisplay(ddlSql));
        return markdown.toString();
    }

    /**
     * DDL 展示：小号灰色字体，过长截断
     *
     * @param ddlSql 清理后的 DDL
     * @return 钉钉 Markdown 片段
     */
    private static String formatDdlDisplay(String ddlSql) {
        String displaySql = ddlSql;
        if (displaySql.length() > DDL_DISPLAY_MAX_LENGTH) {
            displaySql = displaySql.substring(0, DDL_DISPLAY_MAX_LENGTH) + "…（已截断）";
        }
        return "<font size=1 color=#8C8C8C>" + escapeXml(displaySql) + "</font>";
    }

    /**
     * 转义 DDL 中可能破坏 font 标签的字符
     *
     * @param text 原文
     * @return 转义后文本
     */
    private static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /**
     * 清理 DDL 展示文本：去掉客户端注释并压缩空白
     *
     * @param ddlSql 原始 DDL
     * @return 展示用 DDL
     */
    private static String cleanDdlSql(String ddlSql) {
        if (StringUtils.isBlank(ddlSql)) {
            return "-";
        }
        String cleanedSql = SQL_BLOCK_COMMENT_PATTERN.matcher(ddlSql).replaceAll(" ");
        cleanedSql = SQL_LINE_COMMENT_PATTERN.matcher(cleanedSql).replaceAll(" ");
        cleanedSql = MULTI_SPACE_PATTERN.matcher(cleanedSql).replaceAll(" ").trim();
        return StringUtils.defaultIfBlank(cleanedSql, "-");
    }

    /**
     * 变更类型对应着色：DROP/TRUNCATE 红，CREATE 绿，ALTER/RENAME 橙
     *
     * @param changeType 变更类型
     * @return 颜色值
     */
    private static String resolveChangeTypeColor(String changeType) {
        String upperType = StringUtils.upperCase(changeType, Locale.ROOT);
        return switch (upperType) {
            case "CREATE" -> "#00B42A";
            case "DROP", "TRUNCATE" -> "#FF4D4F";
            case "ALTER", "RENAME" -> "#FF7D00";
            default -> "#1677FF";
        };
    }

    /**
     * 钉钉 Markdown 彩色加粗文本
     *
     * @param text  文本
     * @param color 颜色值
     * @return 带颜色标签的文本
     */
    private static String colorText(String text, String color) {
        return "<font color=" + color + ">" + text + "</font>";
    }

    private static String formatEventTime(Long eventTime) {
        if (Objects.isNull(eventTime) || eventTime <= 0L) {
            return DateUtil.now();
        }
        return DateUtil.formatDateTime(new Date(eventTime));
    }

}
