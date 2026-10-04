package com.awe.qd.module.community.dto;

import com.awe.qd.module.community.constant.ContentPublicLevelEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建社区内容请求。
 */
@Data
public class CommunityContentCreateReq {

    /**
     * 内容类型。
     */
    @NotBlank
    private String contentType;

    /**
     * 标题。
     */
    @NotBlank
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
    @NotBlank
    private String body;

    /**
     * 公开级别。
     */
    @NotNull
    private ContentPublicLevelEnum publicLevel;

    /**
     * 发布人昵称。
     */
    private String authorName;
}
