package com.glovoapp.ownership.shared;

import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public final class LazyReference<T> {

    private final Supplier<T> valueSupplier;
    private final SingletonReference<T> reference = new SingletonReference<>();

    public final synchronized T get() {
        return reference.initializeAndGet(valueSupplier);
    }

}
