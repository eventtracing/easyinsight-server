package com.netease.hz.bdms.easyinsight.common.util;

import com.netease.hz.bdms.easyinsight.common.exception.ParamException;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

public class ParamCheckUtil {

    public static void checkOid(String oid) {
        if (StringUtils.isBlank(oid)) {
            throw new ParamException("oid不能为空");
        }
        char[] chars = oid.toCharArray();
        for (char c : chars) {
            if (CharUtils.isAsciiAlpha(c)) {
                continue;
            }
            if (CharUtils.isAsciiNumeric(c)) {
                continue;
            }
            if ('_' == c) {
                continue;
            }
            throw new ParamException("oid包含无效字符：" + c);
        }
    }
}
