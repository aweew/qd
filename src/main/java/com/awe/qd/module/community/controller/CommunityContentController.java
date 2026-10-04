package com.awe.qd.module.community.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.awe.qd.common.api.Result;
import com.awe.qd.module.community.dto.CommunityContentCreateReq;
import com.awe.qd.module.community.dto.CommunityContentResp;
import com.awe.qd.module.community.dto.ShareContentReq;
import com.awe.qd.module.community.service.ICommunityContentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社区内容和分享接口。
 */
@RestController
@RequestMapping("/community/content")
public class CommunityContentController {

    @Resource
    private ICommunityContentService contentService;

    /**
     * 查询公开内容列表。
     *
     * @return 公开内容
     */
    @GetMapping("/public")
    @SaIgnore
    public Result<List<CommunityContentResp>> listPublicContents() {
        return Result.success(contentService.listPublicContents());
    }

    /**
     * 查询公开内容详情。
     *
     * @param contentId 内容主键
     * @return 内容详情
     */
    @GetMapping("/public/{contentId}")
    @SaIgnore
    public Result<CommunityContentResp> getPublicContent(@PathVariable Long contentId) {
        return Result.success(contentService.getPublicContent(contentId));
    }

    /**
     * 记录内容分享。
     *
     * @param contentId 内容主键
     * @param request 分享请求
     * @return 空响应
     */
    @PostMapping("/public/{contentId}/share")
    @SaIgnore
    public Result<Void> recordShare(@PathVariable Long contentId,
                                    @Valid @RequestBody ShareContentReq request) {
        contentService.recordShare(contentId, request);
        return Result.success();
    }

    /**
     * 创建并发布社区内容。
     *
     * @param request 创建请求
     * @return 内容主键
     */
    @PostMapping
    public Result<Long> createContent(@Valid @RequestBody CommunityContentCreateReq request) {
        StpUtil.checkLogin();
        return Result.success(contentService.createContent(request));
    }
}
