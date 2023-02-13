package com.netease.hz.bdms.easyinsight.common.util;

import java.util.*;

/**
 * 树结构的工具类
 *
 * @author wangliangyuan
 * @date 2021-08-04 下午 12:26
 */
public class TreeUtil {

    /**
     * 构建树节点
     *
     * @param treeNodes
     * @param <K>
     * @param <T>       节点必须继承 {@link TreeNode}
     * @return
     */
    public static <K, T extends TreeNode> List<T> build(List<T> treeNodes) {
        List<T> result = new ArrayList<>();

        // list 转 map
        Map<K, T> nodeMap = new LinkedHashMap<>(treeNodes.size());
        for (T treeNode : treeNodes) {
            nodeMap.put((K) treeNode.getCode(), treeNode);
        }

        Collection<T> values = nodeMap.values();

        for (T node : values) {
            T parent = nodeMap.get(node.getParentCode());
            if (parent != null && !(node.getCode().equals(parent.getCode()))) {
                parent.getChildren().add(node);
                continue;
            }

            result.add(node);
        }

        return result;
    }
}
