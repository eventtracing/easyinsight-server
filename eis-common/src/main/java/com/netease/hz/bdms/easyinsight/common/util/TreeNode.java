package com.netease.hz.bdms.easyinsight.common.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形结构的基类
 *
 * @author wangliangyuan
 * @date 2021-08-04 下午 12:27
 */
@Data
public abstract class TreeNode<T, K> {
    /**
     * 唯一标识
     */
    private K code;
    /**
     * 父级唯一标识
     */
    private K parentCode;
    /**
     * 子节点列表
     */
    private List<T> children = new ArrayList<>();
}