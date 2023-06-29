package com.netease.hz.bdms.easyinsight.common.vo.synctree;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

/**
 * 外部搭建对接
 */
@Data
@Accessors(chain = true)
public class SyncObjVO {

    /**
     * {@link com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum}
     */
    private Integer type;

    /**
     * 对象OID，小写字母+下划线组成。
     */
    private String oid;

    /**
     * 对象名，非空
     */
    private String objName;

    /**
     * 图片
     */
    private List<String> images;

    /**
     * 对象描述，可空
     */
    private String description = "";

    /**
     * 终端名，需要在平台内先新建终端
     */
    private String terminalName;

    /**
     * 事件名Set
     */
    private Set<String> eventCodes;

    /**
     * 父对象Set
     */
    private Set<String> parentOids;

    /**
     * 对象参数列表
     */
    private List<String> objParams;
}
