package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 埋点日志中错误信息的类型枚举
 *
 * @author wangliangyuan
 * @date 2021-08-27 上午 10:57
 */
@Getter
@AllArgsConstructor
public enum BuryPointErrorCategoryEnum {

    NODE_NOT_UNIQUE("NodeNotUnique", 41, "节点唯一性检测"),
    NODE_SPM_NOT_UNIQUE("NodeSPMNotUnique", 42, "节点 spm 唯一检测"),
    LOGICAL_MOUNT_ENDLESS_LOOP("LogicalMountEndlessLoop", 43, "逻辑挂载死循环"),

    EVENT_KEY_INVALID("EventKeyInvalid", 51, "事件命名不规范"),
    EVENT_KEY_CONFLICT_WITH_EMBEDDED("EventKeyConflictWithEmbedded", 52, "事件命名跟内部保留字段冲突"),
    PUBLIC_PARAM_INVALID("PublicParamInvalid", 53, "公参命名不规范"),
    USER_PARAM_INVALID("UserParamInvalid", 54, "用户自定义参数命名不规范"),
    PARAM_CONFLICT_WITH_EMBEDDED("ParamConflictWithEmbedded", 55, "参数命名跟内部保留字冲突"),

    OTHER("other", 61, "其它");

    private final String key;
    private final Integer code;
    /**
     * 类别
     */
    private final String category;

    private static final Map<Integer, BuryPointErrorCategoryEnum> CATEGORY_ENUM_MAP = Arrays.stream(values()).collect(Collectors.toMap(BuryPointErrorCategoryEnum::getCode, Function.identity()));

    public static BuryPointErrorCategoryEnum match(Integer code) {
        return CATEGORY_ENUM_MAP.getOrDefault(code, OTHER);
    }
}

