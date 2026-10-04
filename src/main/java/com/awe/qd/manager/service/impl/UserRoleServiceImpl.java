package com.awe.qd.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.awe.qd.manager.domain.userRole.entity.UserRole;
import com.awe.qd.manager.mapper.UserRoleMapper;
import com.awe.qd.manager.service.IUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 系统用户角色服务实现类
 *
 * @author Awe
 * @since 2025-12-11 16:45:02
 */
@Service("userRoleService")
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

}
