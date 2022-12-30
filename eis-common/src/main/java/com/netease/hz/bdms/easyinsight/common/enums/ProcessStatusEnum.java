package com.netease.hz.bdms.easyinsight.common.enums;

import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProcessStatusEnum {
    //未指派
    UNASSIGN(0,"未指派", "待指派"),
    //开始，数仓设计节点
    START(1,"开始", "待设计"),
    //待审核，开发审核节点
    WAIT_VERIFY(2,"待审核", "待审核"),
    //已审核，开发开发节点
    VERIFY_FINISHED(3,"已审核", "待开发"),
    //已完成，测试验证节点
    DEV_FINISHED(4,"已完成", "待测试"),
    //测试通过，开发上线节点
    TEST_FINISHED(5,"测试通过", "待上线"),
    //已上线，流程结束
    ONLINE(6,"已上线", "已上线");

    private Integer state;

    private String desc;

    private String todoDesc;

    public static ProcessStatusEnum fromState(Integer state){
        for(ProcessStatusEnum processStatusEnum:values()){
            if(processStatusEnum.getState().equals(state)){
                return processStatusEnum;
            }
        }
        throw new ServerException(state + "不能转换为ProcessStatusEnum");
    }

}
