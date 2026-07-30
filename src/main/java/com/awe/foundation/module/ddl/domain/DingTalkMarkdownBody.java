package com.awe.foundation.module.ddl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 钉钉机器人 Markdown 消息体
 *
 * @author Awe
 * @since 2026/7/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DingTalkMarkdownBody {

    /**
     * 消息标题（会话列表摘要）
     */
    private String title;

    /**
     * Markdown 正文
     */
    private String text;

}
