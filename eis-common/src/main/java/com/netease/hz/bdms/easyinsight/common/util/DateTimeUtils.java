package com.netease.hz.bdms.easyinsight.common.util;

import lombok.SneakyThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static String getCurrent() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getSimplifiedCurrent() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String getDateString(Date date) {
        return getDateString(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDateString(Date date, String timeFormat) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(timeFormat);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return dtf.format(localDateTime);
    }

    @SneakyThrows
    public static Map<String, String> getOfflineTime(String startTime, String endTime) {
        Map<String, String> map = new HashMap<>();
        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

        Date startDate = ymd.parse(startTime);
        Date endDate = ymd.parse(endTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);//设置起时间
        cal.add(Calendar.DATE, 1);
        map.put("startTime", ymd.format(cal.getTime()));
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(endDate);//设置起时间
        cal1.add(Calendar.DATE, 1);
        map.put("endTime", ymd.format(cal.getTime()));
        return map;
    }
}
