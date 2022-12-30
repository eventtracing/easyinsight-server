package com.netease.hz.bdms.easyinsight.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CollectionUtil {

    /**
     * 得到left-right的结果
     *
     * @param left  左值
     * @param right 右值
     * @return
     */
    public static <T> Collection<T> getDifference(Collection<T> left, Collection<T> right) {
        if (left == null || left.isEmpty()) {
            return null;
        }
        if (right == null || right.isEmpty()) {
            return left;
        }
        List<T> result = new LinkedList<>(left);
        result.removeAll(right);
        return result;
    }

    public static <T> boolean same(Collection<T> left, Collection<T> right) {
        boolean leftIsEmpty = CollectionUtils.isEmpty(left);
        boolean rightIsEmpty = CollectionUtils.isEmpty(right);
        if (leftIsEmpty && rightIsEmpty) {
            return true;
        } else if (leftIsEmpty || rightIsEmpty) {
            return false;
        } else {
            Collection<T> leftDiff = getDifference(left, right);
            Collection<T> rightDiff = getDifference(right, left);
            if (CollectionUtils.isEmpty(leftDiff) && CollectionUtils.isEmpty(rightDiff)) {
                return true;
            } else {
                return false;
            }
        }
    }

}
