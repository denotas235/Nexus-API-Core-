package com.nexuapicore.logging;

import java.util.concurrent.atomic.AtomicInteger;

public class RingBuffer<T> {
    private final T[] buffer;
    private final AtomicInteger head = new AtomicInteger(0);
    private final AtomicInteger tail = new AtomicInteger(0);
    
    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        this.buffer = (T[]) new Object[capacity];
    }
    
    public boolean offer(T item) {
        int next = (head.get() + 1) % buffer.length;
        if (next == tail.get()) return false;
        buffer[head.get()] = item;
        head.set(next);
        return true;
    }
    
    public T poll() {
        if (head.get() == tail.get()) return null;
        T item = buffer[tail.get()];
        tail.set((tail.get() + 1) % buffer.length);
        return item;
    }
}
