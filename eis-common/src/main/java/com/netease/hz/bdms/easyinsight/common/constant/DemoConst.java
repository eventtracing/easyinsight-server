package com.netease.hz.bdms.easyinsight.common.constant;

import com.netease.hz.bdms.easyinsight.common.dto.common.SessionDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;

/**
 * 示例常量，初始化数据时需要
 */
public class DemoConst {

    public static final String SYSTEM_USER_TOKEN = "SYSTEM_USER_TOKEN";
    public static final SessionDTO SYSTEM_SESSION_DTO = new SessionDTO();
    public static final UserDTO SYSTEM_USER_DTO = new UserDTO();
    public static final long SYSTEM_USER_ID = 2147483647L;   // DB无该值
    public static final UserSimpleDTO SYSTEM_SIMPLE_USER_DTO = new UserSimpleDTO();

    public static final long DEMO_DOMAIN_ID = 1L;

    public static final String DEMO_APP_CODE = "init_demo";
    public static final String DEMO_APP_NAME = "DEMO";

    static {
        SYSTEM_SESSION_DTO.setUserId(SYSTEM_USER_ID);
        SYSTEM_SESSION_DTO.setExpireTime(2617587388000L);   // 很久以后
        SYSTEM_SESSION_DTO.setToken(SYSTEM_USER_TOKEN);
        SYSTEM_SESSION_DTO.setDomainId(1L);

        SYSTEM_USER_DTO.setId(SYSTEM_USER_ID);
        SYSTEM_USER_DTO.setEmail("SYSTEM");
        SYSTEM_USER_DTO.setUserName("SYSTEM");

        SYSTEM_SIMPLE_USER_DTO.setId(SYSTEM_USER_ID);
        SYSTEM_SIMPLE_USER_DTO.setEmail("SYSTEM");
        SYSTEM_SIMPLE_USER_DTO.setUserName("SYSTEM");
    }
}
