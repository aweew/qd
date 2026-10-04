package com.awe.qd.manager.controller;

import com.awe.qd.common.api.Result;
import com.awe.qd.common.constant.EnumCollector;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 枚举管理
 *
 * @author Awe
 * @since 2025/12/9 15:32
 */
@RestController
@RequestMapping("/enum")
public class EnumController {

    @Resource
    private EnumCollector enumCollector;

    /**
     * 获取所有枚举列表
     *
     * @return 枚举列表
     */
    @GetMapping("/list")
    public Result<?> listAllEnums() {
        return Result.success(enumCollector.getAllEnums());
    }

}
