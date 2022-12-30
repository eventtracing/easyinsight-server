package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RequirementTypeEnum {

    CREATE(1,"对象新建"),
    PRV_PARAM_CHANGE(2,"私参变更"),
    PUB_PARAM_CHANGE(3,"公参变更"),
    EVT_CHANGE(4,"事件变更"),
    NEW_PARENT(5,"血缘新增"),
    NEW_TERMINAL(6,"终端新增"),
    DELETE_PARENT(7,"血缘下线"),
    DELETE_TERMINAL(8,"终端删除"),
    //为了兼容旧版数据
    PARAM_CHANGE(9,"参数变更"),
    REUSE_CHANGE(10,"复用开发");

    private Integer reqType;

    private String desc;

    public static RequirementTypeEnum fromReqType(Integer reqType){
        for(RequirementTypeEnum requirementType:values()){
            if(requirementType.getReqType().equals(reqType)){
                return requirementType;
            }
        }
        throw new ServerException(reqType + "不能转换为RequirementTypeEnum");
    }

}
