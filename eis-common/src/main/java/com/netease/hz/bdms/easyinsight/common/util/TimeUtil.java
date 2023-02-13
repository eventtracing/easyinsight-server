package com.netease.hz.bdms.easyinsight.common.util;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;


@Slf4j
public class TimeUtil {

    /**
     * 将字符串形式的时间解析为13位时间戳
     *
     * @param timeStr 字符串形式的时间，可能是日期形式的格式化的时间戳串 或 整型 或 长整型
     * @return 13位时间戳
     */
    public static Long parseTimeStr(String timeStr) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }
        Long timestamp = null;
        if (timeStr.contains("-")) {
            String pattern = timeStr.contains(":") ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd";
            try {
                timestamp = new SimpleDateFormat(pattern).parse(timeStr).getTime();
            } catch (ParseException e) {
                log.error("转换日志字符串出错, time={}, ", timeStr, e);
                throw new ServerException("转换日志字符串出错, time=" + timeStr);
            }
        } else {
            if (timeStr.length() == 13) {
                // 13位时间戳，表示以毫秒为单位
                timestamp = Long.parseLong(timeStr);
            } else {
                timestamp = Integer.parseInt(timeStr) * 1000l;
            }
        }
        return timestamp;
    }
}
