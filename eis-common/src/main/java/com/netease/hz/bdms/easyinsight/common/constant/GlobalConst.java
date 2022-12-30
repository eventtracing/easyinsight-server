package com.netease.hz.bdms.easyinsight.common.constant;

import com.google.common.collect.ImmutableMap;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 全局常量
 */
public class GlobalConst {

    private GlobalConst() {}
    /**
     * 支持的优先级
     */
    public static final List<String> PRIORITY_ARRAY = Arrays.asList("P0","P1","P2");
    /**
     * 分页查询映射
     */
    public static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    /**
     * 分页查询映射
     */
    public static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");

    /**
     * 埋点sub_task标题
     */
    //开始
    public static final String TRACKING_START = "【埋点-开始】";
    //待审核
    public static final String TRACKING_WAIT_VRFY = "【埋点-待审核】";
    //已审核
    public static final String TRACKING_VRFY_FINISHED = "【埋点-已审核】";
    //已完成
    public static final String TRACKING_DEV_FINISHED = "【埋点-已完成】";
    //测试通过
    public static final String TRACKING_TEST_FINISHED = "【埋点-测试通过】";

    /**
     * 默认的平台 ID
     */
    public static final Long DEFAULT_PLATFORM_ID = -1L;

    /**
     * 非内置角色的角色等级
     */
    public static final Integer DEFAULT_ROLE_LEVEL_OF_NOT_BUILTIN = Ordered.LOWEST_PRECEDENCE;

    /**
     * 按钮的默认排序值
     */
    public static final Integer DEFAULT_AUTH_SORT_OF_BUTTON = Ordered.LOWEST_PRECEDENCE;

    /**
     * 权限默认的根节点 parentCode
     */
    public static final Integer DEFAULT_PARENT_CODE_OF_ROOT_AUTH = -1;

    /**
     * @ 符号
     */
    public static final String AT = "@";

    /**
     * 默认的系统错误提示信息
     */
    public static final String DEFAULT_INTERNAL_SERVER_ERROR_MESSAGE = "服务器开了会小差，请稍后再试";
}
