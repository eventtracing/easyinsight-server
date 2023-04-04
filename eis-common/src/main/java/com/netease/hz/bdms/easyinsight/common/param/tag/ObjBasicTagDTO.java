package com.netease.hz.bdms.easyinsight.common.param.tag;

import com.netease.hz.bdms.easyinsight.common.enums.ObjSubTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 对象基础标签，根版本无关
 */
@Accessors(chain = true)
@Data
public class ObjBasicTagDTO {

    /**
     * 业务线code
     */
    private String bizGroup;

    /**
     * 业务线名称
     */
    private String bizGroupName;

    /**
     * 元素类型 {@link ObjSubTypeEnum}
     */
    private String objSubType;
}
