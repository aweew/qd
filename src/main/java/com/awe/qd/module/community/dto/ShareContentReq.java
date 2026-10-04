package com.awe.qd.module.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 内容分享请求。
 */
@Data
public class ShareContentReq {

    /**
     * 分享来源。
     */
    @NotBlank
    private String source;
}
