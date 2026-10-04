package com.awe.qd.module.community.dto;

import com.awe.qd.module.community.constant.ContentPublicLevelEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区内容公开响应。
 */
@Data
@Builder
public class CommunityContentResp {

    /**
     * 内容主键。
     */
    private Long id;

    /**
     * 内容类型。
     */
    private String contentType;

    /**
     * 标题。
     */
    private String title;

    /**
     * 摘要。
     */
    private String summary;

    /**
     * 封面地址。
     */
    private String coverUrl;

    /**
     * 正文。
     */
    private String body;

    /**
     * 公开级别。
     */
    private ContentPublicLevelEnum publicLevel;

    /**
     * 作者昵称。
     */
    private String authorName;

    /**
     * 浏览次数。
     */
    private Long viewCount;

    /**
     * 分享次数。
     */
    private Long shareCount;

    /**
     * 发布时间。
     */
    private LocalDateTime publishTime;

    /**
     * 小程序详情页路径。
     */
    private String sharePath;
}
