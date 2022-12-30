package com.netease.hz.bdms.easyinsight.common.obj;

import java.util.Objects;

public class ThreeTuple<A, B, C> {
    private final A first;

    private final B second;

    private final C three;

    public ThreeTuple(A a, B b, C c) {
        this.first = a;
        this.second = b;
        this.three = c;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThree() {
        return three;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThreeTuple<?, ?, ?> that = (ThreeTuple<?, ?, ?>) o;
        return first.equals(that.first) &&
                second.equals(that.second) &&
                three.equals(that.three);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, three);
    }
}
