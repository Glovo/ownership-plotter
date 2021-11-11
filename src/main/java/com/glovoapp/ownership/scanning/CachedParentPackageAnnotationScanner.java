package com.glovoapp.ownership.scanning;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class CachedParentPackageAnnotationScanner <A extends Annotation> implements AnnotationScanner<A> {

    private final ConcurrentHashMap<String, Optional<A>> cache = new ConcurrentHashMap<>();

    private final ParentPackageAnnotationScanner<A> delegate;

    @Override
    public Optional<A> scan(AnnotatedElement element) {
        return PackageScanningUtils.getPackageName(element)
                                   .flatMap(packageName -> cache.computeIfAbsent(packageName,
                                       ignored -> delegate.scan(element)
                                   ));
    }
}
