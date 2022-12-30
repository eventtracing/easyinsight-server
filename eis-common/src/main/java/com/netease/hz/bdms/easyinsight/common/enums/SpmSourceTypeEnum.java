package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpmSourceTypeEnum {
    // 任务
    SCHEDULE(0,"任务同步"),

    // 人工
    AITIFICIAL(1,"手动添加"),

    // 浮层衍生
    POPVOLE(-1,"浮层衍生");


    private Integer status;

    private String desc;

    public static Boolean containsStatus(Integer status){
        for(SpmSourceTypeEnum statusEnum: values()){
            if(statusEnum.getStatus().equals(status)){
                return true;
            }
        }
        return false;
    }

    public static SpmSourceTypeEnum fromStatus(Integer status){
        for(SpmSourceTypeEnum statusEnum: values()){
            if(statusEnum.getStatus().equals(status)){
                return statusEnum;
            }
        }
        throw new ServerException(status + "不能转换为SpmSourceTypeEnum");
    }
}
