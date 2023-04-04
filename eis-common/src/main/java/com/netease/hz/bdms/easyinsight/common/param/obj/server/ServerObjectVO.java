package com.netease.hz.bdms.easyinsight.common.param.obj.server;

import com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 服务端对象新建
 */
@Data
@Accessors(chain = true)
public class ServerObjectVO {

    /**
     * 对象Id
     */
    private Long id;

    /**
     * {@link ObjTypeEnum}
     * 对象类型，只能传ObjTypeEnum.namespace = server的
     */
    private Integer objType;

    /**
     * 服务端事件名（实际上是oid）
     */
    private String serverEventCode;

    /**
     * 中文名称
     */
    private String name;

    /**
     * 对象描述
     */
    private String description;

    /**
     * 标签ID集合
     */
    private List<Long> tagIds;

    /**
     * API信息
     */
    private List<ServerApiInfo> apiInfos;

    /**
     * 服务端埋点参数信息
     */
    private List<ParamBindItermParam> serverParamBindItemParams;

    /**
     * 终端ID
     */
    private Long terminalId;

    /**
     * 需求组ID
     */
    private Long reqPoolId;

    /**
     * 对象优先级
     */
    private String priority;

}
