package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReqTaskStatusEnum {
    // 开始，数仓设计节点
    START(1,"开始"),

    // 待审核，开发审核节点
    WAIT_VRFY(2,"待审核"),

    // 已审核，开发开发节点
    VRFY_FINISHED(3,"已审核"),

    // 已完成，测试验证节点
    DEV_FINISHED(4,"已完成"),

    // 测试通过，开发上线节点
    TEST_FINISHED(5,"测试通过"),

    //已上线，流程结束
    ONLINE(6,"已上线");

    private Integer state;

    private String desc;

    public static ReqTaskStatusEnum fromState(Integer state){
        for(ReqTaskStatusEnum taskStatusEnum:values()){
            if(taskStatusEnum.getState().equals(state)){
                return taskStatusEnum;
            }
        }
        throw new ServerException(state + "不能转换为ReqTaskStatusEnum");
    }

}
