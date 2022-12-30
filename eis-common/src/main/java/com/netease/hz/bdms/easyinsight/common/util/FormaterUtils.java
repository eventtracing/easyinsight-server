package com.netease.hz.bdms.easyinsight.common.util;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FormaterUtils {

    public static final String EMPTY = "NULL";

    public static String convertToReadable(String s) {
        if ("".equals(s)) {
            return EMPTY;
        }
        return s;
    }

    public static String convertToOriginal(String s) {
        if (EMPTY.equals(s)) {
            return "";
        }
        return s;
    }

    public static List<String> convertToOriginal(List<String> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(FormaterUtils::convertToOriginal).collect(Collectors.toList());
    }

    public static Long parseLong(String value) {
        if (StringUtils.isNotBlank(value)) {
            try {
                return Long.parseLong(value);
            } catch (Throwable e) {
                throw new ServerException(value + "不能转换为整型类型");
            }
        } else {
            throw new ServerException(value + "不能转换为整型类型");
        }
    }

}
