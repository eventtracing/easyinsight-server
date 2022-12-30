package com.netease.hz.bdms.easyinsight.common.dto.rbac;

import com.netease.hz.bdms.easyinsight.common.util.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuNodeDTO extends TreeNode<MenuNodeDTO, Integer> {
    private Long id;
    /**
     * 权限类型
     */
    private Integer menuType;
    /**
     * 权限名称
     */
    private String menuName;

    private Integer kind;
    /**
     * 是否选中: true-选中;false-不选中
     */
    private Boolean assigned;
}
