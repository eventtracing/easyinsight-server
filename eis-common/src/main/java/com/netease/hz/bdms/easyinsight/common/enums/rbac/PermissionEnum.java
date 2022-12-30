package com.netease.hz.bdms.easyinsight.common.enums.rbac;

import com.netease.hz.bdms.easyinsight.common.constant.GlobalConst;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统内置的权限
 *
 * @author wangliangyuan
 * @date 2021-08-06 上午 10:16
 */
@Getter
@AllArgsConstructor
public enum PermissionEnum {

    //==================================================================================================================

    BURY_POINT_MANAGEMENT(1, GlobalConst.DEFAULT_PARENT_CODE_OF_ROOT_AUTH,"埋点管理",0,2),

        REQUIREMENT_MANAGEMENT(50,1,"需求管理",0,1),

            REQUIREMENT_MAN(100,50,"需求组",0,1),

                REQUIREMENT_READ(1000,100,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                REQUIREMENT_CREATE(1001,100,"增加需求",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                REQUIREMENT_EDIT(1002,100,"编辑需求",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                REQ_OBJ_CREATE(1003,100,"新建对象",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                REQ_OBJ_EDIT(1004,100,"编辑对象",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                DESIGN_DONE(1005,100,"设计完成",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                VERIFY_ACK(1006,100,"审核确认",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                DEVELOP_DONE(1007,100,"开发完成",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                TEST_DONE(1008,100,"测试完成",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

            TRACKER_ASSIGNMENT(106, 50, "埋点任务", 0, 2),

            VERSION_MAN(101,50,"发布管理",0,3),
                VERSION_READ(1009,101,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                VERSION_EDIT(1010,101,"编辑版本号",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                VERSION_RELEASE(1011,101,"发布上线",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


        OBJECT_MAN(51,1,"对象管理",0,2),

                OBJ_READ(1012,51,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                OBJ_CREATE(1013,51,"新建对象",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                OBJ_EDIT(1014,51,"变更对象",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


    EVENT_MAN(64, 1, "已上线事件", 0, 3),

    SPM_MAN(63, 1, "SPM管理", 0, 4),
        SPM_READ(1068, 63, "页面查询权限",1,  GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


    METADATA_MAN(52,1,"元数据管理",0,5),

            PARAM_MAN(102,52,"参数管理",0,1),

                PARAM_READ(1015,102,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_CREATE(1016,102,"添加参数",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_EDIT(1017,102,"编辑参数",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_DELETE(1020,102,"删除参数",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_MEANING_CREATE(1018,102,"添加参数含义",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                VALUE_MAN(1019,102,"取值管理",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_MEANING_EDIT(1065,102,"编辑参数含义",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_MEANING_DELETE(1066,102,"删除参数含义",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

            PARAM_TEMPLATE(103,52,"参数模版",0,2),

                PARAM_TEMPLATE_READ(1021,103,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_TEMPLATE_CREATE(1022,103,"添加模版",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_TEMPLATE_EDIT(1023,103,"编辑模版",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_TEMPLATE_COPY(1024,103,"复制模版",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                PARAM_TEMPLATE_DELETE(1025,103,"删除模版",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


            EVENT_TYPE(104,52,"事件类型",0,3),

                EVENT_TYPE_READ(1026,104,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                EVENT_TYPE_CREATE(1027,104,"添加事件类型",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                EVENT_TYPE_EDIT(1028,104,"编辑事件类型",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                EVENT_TYPE_DELETE(1029,104,"删除事件类型",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                EVENT_TYPE_PARAM_MAN(1030,104,"事件类型-参数管理",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


            TERMINAL_MAN(105,52,"终端管理",0,4),

                TERMINAL_READ(1031,105,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                TERMINAL_CREATE(1032,105,"添加终端类型",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                TERMINAL_EDIT(1033,105,"编辑终端类型",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                TERMINAL_DELETE(1034,105,"删除终端类型",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
                TERMINAL_PARAM_MAN(1035,105,"终端类型-参数管理",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

            RULE_MAN(107,52,"规则管理",0,4),
                RULE_MAN_EDIT(1071,107,"编辑规则管理",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
    //==================================================================================================================

    BURY_POINT_TEST(2, GlobalConst.DEFAULT_PARENT_CODE_OF_ROOT_AUTH,"埋点测试",0,3),

        REALTIME_TEST(53,2,"实时测试",0,1),

            RULE_CHECK(1036,53,"规则校验",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            CHECK_RES_SAVE(1037,53,"校验结果保存",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            REALTIME_LOG(1038,53,"实时日志",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


        DEMAND_TEST(54,2,"需求测试",0,2),

        TEST_RECORD(88,2,"测试记录",0,3),
    //==================================================================================================================

    PRODUCT_MANAGEMENT(4, GlobalConst.DEFAULT_PARENT_CODE_OF_ROOT_AUTH,"产品管理",0,4),

        PRODUCT_INFO(58,4,"产品信息",0,1),

        PRODUCT_MEMBER_MAN(59,4,"成员管理",0,2),

            PRODUCT_MEMBER_READ(1043,59,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PRODUCT_MEMBER_CREATE(1044,59,"添加成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PRODUCT_MEMBER_REMOVE(1045,59,"移除成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PRODUCT_MEMBER_EDIT(1046,59,"编辑成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


        ROLE_MAN(60,4,"角色管理",0,3),

            ROLE_READ(1047,60,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            ROLE_CREATE(1048,60,"新增角色",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            ROLE_EDIT(1049,60,"编辑角色",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            ROLE_DELETE(1050,60,"删除角色",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            ROLE_MEMBER_CREATE(1051,60,"添加成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            ROLE_MEMBER_REMOVE(1052,60,"移除成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            ROLE_AUTH(1053,60,"功能权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

        NOTIFY_MAN(62,4,"通知管理",0,4),
            NOTIFY_READ(1067,62,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

    //==================================================================================================================


    DOMAIN_MANAGEMENT(3, GlobalConst.DEFAULT_PARENT_CODE_OF_ROOT_AUTH,"域管理",0,5),

        DOMAIN_INFO(55,3,"域信息",0,1),

            DOMAIN_INFO_READ(1059,55,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


        DOMAIN_MEMBER_MAN(56,3,"成员管理",0,2),

            DOMAIN_MEMBER_READ(1039,56,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            DOMAIN_MEMBER_CREATE(1040,56,"新增成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            DOMAIN_MEMBER_REMOVE(1041,56,"移除成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            DOMAIN_MEMBER_EDIT(1042,56,"编辑成员",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),


        PRODUCT_CONFIG(57,3,"产品配置",0,3),

            PRODUCT_READ(1060,57,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PRODUCT_CREATE(1061,57,"新建产品",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PRODUCT_EDIT(1062,57,"编辑产品",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PRODUCT_DELETE(1063,57,"删除产品",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

            @Deprecated //没用到
            PRODUCT_INFO_READ(1064,57,"页面查询",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

    //==================================================================================================================

    PLATFORM_MANAGEMENT(5, GlobalConst.DEFAULT_PARENT_CODE_OF_ROOT_AUTH,"平台管理",0,6),

        DOMAIN_CONFIG(61,5,"域配置",0,1),

            PLATFORM_MAN_READ(1054,61,"页面查询权限",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PLATFORM_DOMAIN_CREATE(1055,61,"新建域",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PLATFORM_DOMAIN_EDIT(1056,61,"编辑域",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),
            PLATFORM_DOMAIN_DELETE(1057,61,"删除域",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON),

            @Deprecated //没用到
            ACCESS_DOMAIN(1058,61,"访问该域",1, GlobalConst.DEFAULT_AUTH_SORT_OF_BUTTON);

    private final Integer code;
    private final Integer parentCode;
    private final String permissionName;
    /**
     * {@link AuthType}
     */
    private final Integer authType;

    private final Integer authSort;

}
