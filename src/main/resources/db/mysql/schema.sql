CREATE TABLE community_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
    title VARCHAR(160) NOT NULL COMMENT '内容标题',
    summary VARCHAR(500) COMMENT '分享卡片摘要',
    cover_url VARCHAR(500) COMMENT '封面地址',
    body TEXT NOT NULL COMMENT '正文内容',
    public_level VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT '公开级别：PUBLIC公开，RESIDENT_ONLY居民可见，ADMIN_ONLY管理员可见',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '发布状态：DRAFT草稿，PUBLISHED已发布，OFFLINE已下架',
    author_name VARCHAR(64) COMMENT '发布人昵称',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    share_count BIGINT NOT NULL DEFAULT 0 COMMENT '分享次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    KEY idx_community_content_public (status, public_level, deleted, create_time)
) COMMENT='社区可发布内容表';

CREATE TABLE community_share_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    content_id BIGINT NOT NULL COMMENT '被分享内容ID',
    source VARCHAR(20) NOT NULL COMMENT '分享来源：FRIEND好友或群聊，TIMELINE朋友圈',
    visitor_key VARCHAR(128) COMMENT '访问者标识，匿名访问不记录个人身份',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_community_share_content (content_id, create_time)
) COMMENT='社区内容分享记录表';
