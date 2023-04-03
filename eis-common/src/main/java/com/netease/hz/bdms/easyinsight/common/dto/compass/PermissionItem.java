package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangyongliang
 * @version 1.0.0
 * @ClassName PermissionItem.java
 * @Description TODO
 * @createTime 2023/3/28 22:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionItem {

    /**
     * 被授权对象
     */
    private String object;

    /**
     * 资源Key
     */
    private String resourceKey;

    /**
     * 操作列表
     */
    private List<String> actionList;

    /**
     * 节点类型
     */
    private String nodeType = "LEAF";
}
