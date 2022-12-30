package com.netease.hz.bdms.easyinsight.common.obj;

import java.util.Objects;

public class TwoTuple<A, B> {
    private final A first;

    private final B second;

    public TwoTuple(A a, B b) {
        this.first = a;
        this.second = b;
    }

    @Override
    public boolean equals(Object o) {
        // 如果两个引用指向同一个对象，返回true
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TwoTuple<?, ?> twoTuple = (TwoTuple<?, ?>) o;
        return first.equals(twoTuple.first) &&
                second.equals(twoTuple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}
