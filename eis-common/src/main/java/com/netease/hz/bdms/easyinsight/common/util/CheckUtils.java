package com.netease.hz.bdms.easyinsight.common.util;

import org.apache.commons.lang3.StringUtils;

public class CheckUtils {
  public static boolean isEmail(String email) {
    if (StringUtils.isBlank(email)) {
      return false;
    }

    return email.matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+");
  }
}
