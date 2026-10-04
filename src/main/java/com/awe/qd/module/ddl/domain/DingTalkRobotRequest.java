package com.awe.qd.module.ddl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 钉钉自定义机器人请求体
 *
 * @author Awe
 * @since 2026/7/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DingTalkRobotRequest {

    /**
     * 消息类型，固定 markdown
     */
    private String msgtype;

    /**
     * Markdown 内容
     */
    private DingTalkMarkdownBody markdown;

}
