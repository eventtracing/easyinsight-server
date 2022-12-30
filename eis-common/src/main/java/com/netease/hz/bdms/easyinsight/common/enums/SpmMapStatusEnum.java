package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: xumengqiang
 * @date: 2021/11/9 14:54
 */

@Getter
@AllArgsConstructor
public enum SpmMapStatusEnum {
    // 已废弃
    ABANDON(0,"已废弃"),

    // 待确认
    UNCONFIRMED(1,"待确认"),

    // 双打预发
    DOUBLE_PRETEST(2,"双打预发"),

    // 单打预发
    SINGLE_PRETEST(3, "单打预发"),

    // 上线
    DEPLOY(4, "上线");

    // 这里的id与报警系统中的支持的channel_id相对应
    private Integer status;

    private String desc;

    public static Boolean containsStatus(Integer status){
        for(SpmMapStatusEnum statusEnum: values()){
            if(statusEnum.getStatus().equals(status)){
                return true;
            }
        }
        return false;
    }

    public static SpmMapStatusEnum fromStatus(Integer status){
        for(SpmMapStatusEnum statusEnum: values()){
            if(statusEnum.getStatus().equals(status)){
                return statusEnum;
            }
        }
        throw new ServerException(status + "不能转换为SpmMapStatusEnum");
    }
}
