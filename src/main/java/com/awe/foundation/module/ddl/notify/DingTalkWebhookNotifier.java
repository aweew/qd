package com.awe.foundation.module.ddl.notify;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.module.ddl.config.properties.DdlDingtalkProperties;
import com.awe.foundation.module.ddl.domain.DdlChangeMessage;
import com.awe.foundation.module.ddl.domain.DingTalkMarkdownBody;
import com.awe.foundation.module.ddl.domain.DingTalkRobotRequest;
import com.awe.foundation.module.ddl.domain.DingTalkRobotResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * 钉钉自定义机器人 Webhook 通知（Markdown）
 *
 * @author Awe
 * @since 2026/7/30
 */
@Slf4j
@Component
public class DingTalkWebhookNotifier {

    @Resource
    private DdlDingtalkProperties ddlDingtalkProperties;

    /**
     * 发送 DDL 变更钉钉 Markdown 通知
     *
     * @param message DDL 变更消息
     */
    public void notifyDdlChange(DdlChangeMessage message) {
        if (Objects.isNull(message) || !ddlDingtalkProperties.isEnabled()) {
            return;
        }
        String webhookUrl = ddlDingtalkProperties.getWebhookUrl();
        if (StringUtils.isBlank(webhookUrl)) {
            log.warn("钉钉 Webhook 未配置，跳过 DDL 通知，database={}, ddl={}",
                    message.getDatabase(), message.getDdlSql());
            return;
        }

        DingTalkRobotRequest requestBody = DingTalkRobotRequest.builder()
                .msgtype("markdown")
                .markdown(DingTalkMarkdownBody.builder()
                        .title(DdlChangeMarkdownBuilder.buildTitle(message))
                        .text(DdlChangeMarkdownBuilder.buildText(message))
                        .build())
                .build();

        String signedUrl = buildSignedUrl(webhookUrl, ddlDingtalkProperties.getSecret());
        try {
            HttpResponse response = HttpRequest.post(signedUrl)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(JsonUtils.toJsonString(requestBody))
                    .timeout(5000)
                    .execute();
            String responseBody = response.body();
            if (!response.isOk()) {
                log.error("钉钉 DDL 通知 HTTP 失败，status={}, body={}, database={}",
                        response.getStatus(), responseBody, message.getDatabase());
                return;
            }
            DingTalkRobotResponse robotResponse = JsonUtils.parseObject(responseBody, DingTalkRobotResponse.class);
            if (Objects.isNull(robotResponse) || !Objects.equals(robotResponse.getErrcode(), 0)) {
                log.error("钉钉 DDL 通知业务失败，body={}, database={}", responseBody, message.getDatabase());
                return;
            }
            log.info("钉钉 DDL 通知发送成功，database={}, changeType={}, objectName={}",
                    message.getDatabase(), message.getChangeType(), message.getObjectName());
        } catch (Exception e) {
            log.error("钉钉 DDL 通知发送异常，database={}, ddl={}",
                    message.getDatabase(), message.getDdlSql(), e);
        }
    }

    /**
     * 按钉钉加签规则拼接 timestamp / sign
     *
     * @param webhookUrl 原始 Webhook 地址
     * @param secret     加签密钥
     * @return 带签名的请求地址
     */
    private String buildSignedUrl(String webhookUrl, String secret) {
        if (StringUtils.isBlank(secret)) {
            return webhookUrl;
        }
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        byte[] signBytes = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8))
                .digest(stringToSign);
        String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signBytes), StandardCharsets.UTF_8);
        String connector = webhookUrl.contains("?") ? "&" : "?";
        return webhookUrl + connector + "timestamp=" + timestamp + "&sign=" + sign;
    }

}
