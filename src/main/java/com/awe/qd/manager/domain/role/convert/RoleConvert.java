package com.awe.qd.manager.domain.role.convert;

import com.awe.qd.manager.domain.role.entity.Role;
import com.awe.qd.manager.domain.role.dto.req.RoleAddReq;
import com.awe.qd.manager.domain.role.dto.req.RoleUpdateReq;
import com.awe.qd.manager.domain.role.dto.req.RoleReq;
import com.awe.qd.manager.domain.role.dto.resp.RoleResp;
import com.awe.qd.common.convert.BaseConvert;
import org.mapstruct.Mapper;

/**
 * 系统角色转换器
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
@Mapper(componentModel = "spring")
public interface RoleConvert extends BaseConvert<Role, RoleAddReq, RoleUpdateReq, RoleReq, RoleResp> {

}
