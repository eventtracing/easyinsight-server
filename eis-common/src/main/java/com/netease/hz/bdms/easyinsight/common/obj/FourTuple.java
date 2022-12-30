package com.netease.hz.bdms.easyinsight.common.obj;

import java.util.Objects;

/**
 * @author: xumengqiang
 * @date: 2021/12/3 11:10
 */
public class FourTuple<A, B, C, D>{

    private final A first;

    private final B second;

    private final C three;

    private final D four;

    public FourTuple(A a, B b, C c, D d){
        this.first = a;
        this.second = b;
        this.three = c;
        this.four = d;
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

    public D getFour() {
        return four;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FourTuple<?, ?, ?, ?> fourTuple = (FourTuple<?, ?, ?, ?>) o;
        return first.equals(fourTuple.first) && second.equals(fourTuple.second)
                && three.equals(fourTuple.three) && four.equals(fourTuple.four);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, three, four);
    }
}
