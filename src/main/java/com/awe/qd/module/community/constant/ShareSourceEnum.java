package com.awe.qd.module.community.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 微信分享来源。
 */
@Getter
@AllArgsConstructor
public enum ShareSourceEnum {

    /**
     * 好友或群聊转发。
     */
    FRIEND("FRIEND", "好友"),

    /**
     * 朋友圈分享或海报。
     */
    TIMELINE("TIMELINE", "朋友圈");

    private final String code;

    private final String description;

    /**
     * 根据编码查找分享来源。
     *
     * @param code 来源编码
     * @return 来源枚举，不存在时返回空
     */
    public static ShareSourceEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(source -> source.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
