package com.netease.hz.bdms.easyinsight.common.bo.lineage;

import com.netease.hz.bdms.easyinsight.common.enums.LineageTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/12/24 17:33
 */
@Data
public class Node {
    public Node(Long objId){
        this.objId = objId;
        this.type = LineageTypeEnum.BASE.getType();
    }

    /**
     * 对象ID
     */
    Long objId;

    /**
     * 对象类型
     */
    Integer type;

    /**
     * 子节点
     */
    List<Node> children;

    /**
     * 业务线
     */
    private String bizGroup;
}
