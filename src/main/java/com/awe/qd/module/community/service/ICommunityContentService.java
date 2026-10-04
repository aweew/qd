package com.awe.qd.module.community.service;

import com.awe.qd.module.community.dto.CommunityContentCreateReq;
import com.awe.qd.module.community.dto.CommunityContentResp;
import com.awe.qd.module.community.dto.ShareContentReq;

import java.util.List;

/**
 * 社区内容服务。
 */
public interface ICommunityContentService {

    /**
     * 查询公开内容列表。
     *
     * @return 已发布且公开的内容
     */
    List<CommunityContentResp> listPublicContents();

    /**
     * 查询公开内容详情并记录浏览。
     *
     * @param contentId 内容主键
     * @return 内容详情
     */
    CommunityContentResp getPublicContent(Long contentId);

    /**
     * 记录一次分享。
     *
     * @param contentId 内容主键
     * @param request 分享请求
     */
    void recordShare(Long contentId, ShareContentReq request);

    /**
     * 创建社区内容。
     *
     * @param request 创建请求
     * @return 内容主键
     */
    Long createContent(CommunityContentCreateReq request);
}
