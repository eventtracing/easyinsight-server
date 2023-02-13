package com.netease.hz.bdms.easyinsight.common.dto.obj;

import com.netease.hz.bdms.easyinsight.common.param.tag.ObjBasicTagDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 桥梁类型的对象EXT字段定义
 */
@Data
@Accessors(chain = true)
public class ObjectExtDTO {

    /**
     * 子空间ID，桥梁类型对象使用
     */
    private Long subAppId;

    /**
     * 子空间ID下的terminal ID，桥梁类型对象使用，如果为0：挂载本端，否则挂载指定端
     */
    private Long subTerminalId;

    /**
     * 对象基本标签
     */
    private ObjBasicTagDTO basicTag;

    /**
     * 是否解析CID信息
     */
    private boolean analyseCid;
}
