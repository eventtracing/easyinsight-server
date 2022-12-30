package com.netease.hz.bdms.easyinsight.common.util;

import com.netease.hz.bdms.easyinsight.common.SysProperties;
import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DomainUrlUtils {

  /**
   * 从访问域名中找出域的code值
   *
   * @param serverName 访问域名
   * @return 域的code值
   */
  public String getDomainCodeByServerName(SysProperties sysProperties, String serverName) {
    if (StringUtils.isNotBlank(serverName)) {
      String appIndexSuffix = sysProperties.getAuth().getAppIndexSuffix();
      String domainCode = serverName.replace(appIndexSuffix, "");
      return domainCode;
    }
    throw new ServerException("请求地址不能为空, serverName=" + serverName);
  }

  /**
   * 获取办公网地址
   *
   * @param serverName 访问域名地址
   * @return 办公网地址
   */
  public String getAppIndexOfficeNetworkAddress(SysProperties sysProperties, String serverName) {
    String domainCode = getDomainCodeByServerName(sysProperties, serverName);
    String appIndex = sysProperties.getAuth().getAppIndex().replace("*", domainCode);
    return appIndex;
  }

  /**
   * 获取机房网地址
   *
   * @param serverName 访问域名地址
   * @return 机房网地址
   */
  public String getAppIndexComputeNetworkAddress(SysProperties sysProperties, String serverName) {
    return sysProperties.getAuth().getAppClearUri();
//    String domainCode = getDomainCodeByServerName(serverName);
//    return sysProperties.getAuth().getAppClearUri().replace("\\*", domainCode);
  }
}
