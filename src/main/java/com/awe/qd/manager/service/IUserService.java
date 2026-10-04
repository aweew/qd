package com.awe.qd.manager.service;

import com.awe.qd.common.api.Result;
import com.awe.qd.manager.domain.auth.dto.resp.UserInfoResp;
import com.awe.qd.manager.domain.user.dto.resp.UserResp;
import com.baomidou.mybatisplus.extension.service.IService;
import com.awe.qd.manager.domain.user.entity.User;

/**
 * 系统用户服务接口
 *
 * @author Awe
 * @since 2025-12-10 16:03:20
 */
public interface IUserService extends IService<User> {

    User getByPhone(String phone);

    UserInfoResp getUserInfo(Long userId);

}
