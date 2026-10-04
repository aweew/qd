package com.awe.qd.module.community.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 社区内容公开级别。
 */
@Getter
@AllArgsConstructor
public enum ContentPublicLevelEnum {

    /**
     * 无需登录即可查看。
     */
    PUBLIC("PUBLIC", "公开"),

    /**
     * 仅认证居民可查看。
     */
    RESIDENT_ONLY("RESIDENT_ONLY", "居民可见"),

    /**
     * 仅管理员可查看。
     */
    ADMIN_ONLY("ADMIN_ONLY", "管理员可见");

    @EnumValue
    private final String code;

    private final String description;
}
