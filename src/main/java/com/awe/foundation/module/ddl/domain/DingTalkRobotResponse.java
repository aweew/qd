package com.awe.foundation.module.ddl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 钉钉机器人接口响应
 *
 * @author Awe
 * @since 2026/7/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DingTalkRobotResponse {

    /**
     * 错误码，0 表示成功
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

}
