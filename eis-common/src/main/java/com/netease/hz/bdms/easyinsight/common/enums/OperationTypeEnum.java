package com.netease.hz.bdms.easyinsight.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationTypeEnum {

    /**
     * 新增
     */
    CREATE(1),
    /**
     * 变更
     */
    CHANGE(2),
    /**
     * 新增终端
     */
    TERMINAL_ADD(3),
    /**
     * 删除终端
     */
    TERMINAL_DELETE(4),
    /**
     * 复用开发
     *
     */
    REUSER(5);

    private Integer operationType;

    public static OperationTypeEnum fromOperationType(Integer type){
        for (OperationTypeEnum typeEnum : OperationTypeEnum.values()) {
            if(typeEnum.getOperationType().equals(type)){
                return typeEnum;
            }
        }
        throw new RuntimeException("无法转化成OperationTypeEnum类型");
    }

}
