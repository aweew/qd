package com.awe.qd.common.util;

import java.time.LocalDateTime;

/**
 * 日期工具类。
 */
public final class DateUtils {

    private DateUtils() {
    }

    /**
     * 获取当前业务时间。
     *
     * @return 当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
