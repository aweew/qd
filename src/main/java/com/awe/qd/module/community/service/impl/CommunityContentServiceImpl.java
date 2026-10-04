package com.awe.qd.module.community.service.impl;

import com.awe.qd.common.exception.BusinessException;
import com.awe.qd.common.util.DateUtils;
import com.awe.qd.module.community.constant.CommunityContentStatusEnum;
import com.awe.qd.module.community.constant.ContentPublicLevelEnum;
import com.awe.qd.module.community.constant.ShareSourceEnum;
import com.awe.qd.module.community.dto.CommunityContentCreateReq;
import com.awe.qd.module.community.dto.CommunityContentResp;
import com.awe.qd.module.community.dto.ShareContentReq;
import com.awe.qd.module.community.entity.CommunityContent;
import com.awe.qd.module.community.entity.ShareRecord;
import com.awe.qd.module.community.mapper.CommunityContentMapper;
import com.awe.qd.module.community.mapper.ShareRecordMapper;
import com.awe.qd.module.community.service.ICommunityContentService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 社区内容服务实现。
 */
@Service
public class CommunityContentServiceImpl extends ServiceImpl<CommunityContentMapper, CommunityContent>
        implements ICommunityContentService {

    @Resource
    private ShareRecordMapper shareRecordMapper;

    /**
     * 查询公开内容列表。
     *
     * @return 已发布且公开的内容
     */
    @Override
    public List<CommunityContentResp> listPublicContents() {
        List<CommunityContent> contents = list(Wrappers.<CommunityContent>lambdaQuery()
                .eq(CommunityContent::getStatus, CommunityContentStatusEnum.PUBLISHED)
                .eq(CommunityContent::getPublicLevel, ContentPublicLevelEnum.PUBLIC)
                .orderByDesc(CommunityContent::getCreateTime));
        return contents.stream().map(this::toResponse).toList();
    }

    /**
     * 查询公开内容详情并记录浏览。
     *
     * @param contentId 内容主键
     * @return 内容详情
     */
    @Override
    @Transactional
    public CommunityContentResp getPublicContent(Long contentId) {
        CommunityContent content = getOne(Wrappers.<CommunityContent>lambdaQuery()
                .eq(CommunityContent::getId, contentId)
                .eq(CommunityContent::getStatus, CommunityContentStatusEnum.PUBLISHED)
                .eq(CommunityContent::getPublicLevel, ContentPublicLevelEnum.PUBLIC));
        if (Objects.isNull(content)) {
            throw new BusinessException("公开内容不存在或已下架");
        }
        content.setViewCount((Objects.isNull(content.getViewCount()) ? 0L : content.getViewCount()) + 1L);
        updateById(content);
        return toResponse(content);
    }

    /**
     * 记录一次分享。
     *
     * @param contentId 内容主键
     * @param request 分享请求
     */
    @Override
    @Transactional
    public void recordShare(Long contentId, ShareContentReq request) {
        CommunityContent content = getOne(Wrappers.<CommunityContent>lambdaQuery()
                .eq(CommunityContent::getId, contentId)
                .eq(CommunityContent::getStatus, CommunityContentStatusEnum.PUBLISHED)
                .eq(CommunityContent::getPublicLevel, ContentPublicLevelEnum.PUBLIC));
        if (Objects.isNull(content)) {
            throw new BusinessException("公开内容不存在或已下架");
        }
        if (Objects.isNull(ShareSourceEnum.fromCode(request.getSource()))) {
            throw new BusinessException("不支持的分享来源");
        }
        ShareRecord shareRecord = ShareRecord.builder()
                .contentId(contentId)
                .source(request.getSource())
                .createTime(DateUtils.now())
                .build();
        shareRecordMapper.insert(shareRecord);
        content.setShareCount((Objects.isNull(content.getShareCount()) ? 0L : content.getShareCount()) + 1L);
        updateById(content);
    }

    /**
     * 创建社区内容。
     *
     * @param request 创建请求
     * @return 内容主键
     */
    @Override
    public Long createContent(CommunityContentCreateReq request) {
        CommunityContent content = CommunityContent.builder()
                .contentType(request.getContentType())
                .title(request.getTitle())
                .summary(request.getSummary())
                .coverUrl(request.getCoverUrl())
                .body(request.getBody())
                .publicLevel(request.getPublicLevel())
                .status(CommunityContentStatusEnum.PUBLISHED)
                .authorName(request.getAuthorName())
                .viewCount(0L)
                .shareCount(0L)
                .createTime(DateUtils.now())
                .updateTime(DateUtils.now())
                .deleted(0)
                .build();
        save(content);
        return content.getId();
    }

    private CommunityContentResp toResponse(CommunityContent content) {
        return CommunityContentResp.builder()
                .id(content.getId())
                .contentType(content.getContentType())
                .title(content.getTitle())
                .summary(content.getSummary())
                .coverUrl(content.getCoverUrl())
                .body(content.getBody())
                .publicLevel(content.getPublicLevel())
                .authorName(content.getAuthorName())
                .viewCount(content.getViewCount())
                .shareCount(content.getShareCount())
                .publishTime(content.getCreateTime())
                .sharePath("/pages/content/detail?id=" + content.getId())
                .build();
    }
}
