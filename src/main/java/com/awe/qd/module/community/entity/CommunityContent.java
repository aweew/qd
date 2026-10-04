package com.awe.qd.module.community.entity;

import com.awe.qd.module.community.constant.CommunityContentStatusEnum;
import com.awe.qd.module.community.constant.ContentPublicLevelEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可发布和分享的社区内容。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("community_content")
public class CommunityContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 3102600914349248840L;

    /**
     * 内容主键。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 内容类型，例如 MEMORY_ARTICLE、MEMORY_PHOTO、ANNOUNCEMENT。
     */
    private String contentType;

    /**
     * 标题。
     */
    private String title;

    /**
     * 分享卡片摘要。
     */
    private String summary;

    /**
     * 封面地址。
     */
    private String coverUrl;

    /**
     * 正文内容。
     */
    private String body;

    /**
     * 内容公开级别。
     */
    private ContentPublicLevelEnum publicLevel;

    /**
     * 发布状态。
     */
    private CommunityContentStatusEnum status;

    /**
     * 发布人昵称。
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
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识。
     */
    @TableLogic
    private Integer deleted;
}
