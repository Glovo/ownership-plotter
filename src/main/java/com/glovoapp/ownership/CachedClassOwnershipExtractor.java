package com.glovoapp.ownership;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class CachedClassOwnershipExtractor implements ClassOwnershipExtractor {

    private final ConcurrentHashMap<Class<?>, Optional<ClassOwnership>> ownershipCache = new ConcurrentHashMap<>();

    private final ClassOwnershipExtractor delegate;

    @Override
    public final Optional<ClassOwnership> getOwnershipOf(final Class<?> aClass) {
        return ownershipCache.computeIfAbsent(aClass, delegate::getOwnershipOf);
    }

}
