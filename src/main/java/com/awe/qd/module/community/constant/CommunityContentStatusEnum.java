package com.awe.qd.module.community.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 社区内容发布状态。
 */
@Getter
@AllArgsConstructor
public enum CommunityContentStatusEnum {

    /**
     * 草稿。
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 已发布。
     */
    PUBLISHED("PUBLISHED", "已发布"),

    /**
     * 已下架。
     */
    OFFLINE("OFFLINE", "已下架");

    @EnumValue
    private final String code;

    private final String description;
}
