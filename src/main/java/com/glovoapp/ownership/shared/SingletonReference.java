package com.glovoapp.ownership.shared;

import java.util.function.Supplier;

public final class SingletonReference<T> {

    private volatile boolean hasBeenInitialized = false;
    private volatile T value;

    public final synchronized T initializeAndGet(final Supplier<T> supplier) {
        if (!hasBeenInitialized) {
            value = supplier.get();
            hasBeenInitialized = true;
        }
        return value;
    }

}
