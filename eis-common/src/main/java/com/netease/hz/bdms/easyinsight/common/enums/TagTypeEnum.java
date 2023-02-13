package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标签类型枚举
 *
 * @author: xumengqiang
 * @date: 2021/11/10 10:07
 */

@Getter
@AllArgsConstructor
public enum TagTypeEnum {
    // 对象 标签类型
    OBJ_TAG(1, "对象类型"),
    // SPM 标签类型
    SPM_TAG(2, "SPM类型");

    private Integer type;

    private String desc;

    public static Boolean containsType(Integer type){
        for (TagTypeEnum typeEnum : TagTypeEnum.values()) {
            if(typeEnum.getType().equals(type)){
                return true;
            }
        }
        return false;
    }

    public static TagTypeEnum fromType(Integer type){
        for (TagTypeEnum typeEnum : TagTypeEnum.values()) {
            if(typeEnum.getType().equals(type)){
                return typeEnum;
            }
        }
        throw new ServerException(type + "不能转换为TagTypeEnum");
    }

}
