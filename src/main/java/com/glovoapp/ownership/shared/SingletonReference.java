package com.glovoapp.ownership.shared;

import java.util.function.Supplier;

public final class SingletonReference<T> {

    private volatile T object = null;

    public final synchronized T get(final Supplier<T> supplier) {
        if (object == null) {
            object = supplier.get();
        }
        return object;
    }

}
