package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 血缘类型
 *
 * @author: xumengqiang
 * @date: 2021/12/31 10:39
 */

@Getter
@AllArgsConstructor
public enum LineageTypeEnum {
    BASE(0, "基础血缘"),

    ADDED(1, "新增血缘"),

    DELETED(2, "删除血缘");

    private Integer type;

    private String desc;

    public static Boolean containsType(Integer type){
        for (LineageTypeEnum typeEnum : LineageTypeEnum.values()) {
            if(typeEnum.getType().equals(type)){
                return true;
            }
        }
        return false;
    }

    public static LineageTypeEnum fromType(Integer type){
        for (LineageTypeEnum typeEnum : LineageTypeEnum.values()) {
            if(typeEnum.getType().equals(type)){
                return typeEnum;
            }
        }
        throw new ServerException(type + "不能转换为LineageTypeEnum");
    }

}
