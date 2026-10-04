package com.awe.qd.module.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区内容传播记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("community_share_record")
public class ShareRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = -4892640847473642662L;

    /**
     * 记录主键。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被分享内容主键。
     */
    private Long contentId;

    /**
     * 分享来源：FRIEND、GROUP、TIMELINE。
     */
    private String source;

    /**
     * 访问者标识，匿名访问不记录个人身份。
     */
    private String visitorKey;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
