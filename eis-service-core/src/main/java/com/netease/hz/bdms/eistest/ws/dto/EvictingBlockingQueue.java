package com.netease.hz.bdms.eistest.ws.dto;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author sguo
 */
public class EvictingBlockingQueue<E> extends ArrayBlockingQueue<E> {
    public EvictingBlockingQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean offer(E e) {
        while (!super.offer(e)) {
            poll();
        }
        return true;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        while (!super.offer(e, timeout, unit)) {
            poll();
        }
        return true;
    }
}