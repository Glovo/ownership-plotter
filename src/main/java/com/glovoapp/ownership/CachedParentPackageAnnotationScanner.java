package com.glovoapp.ownership;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class CachedParentPackageAnnotationScanner <A extends Annotation> implements AnnotationScanner <A>{

    private final ConcurrentHashMap<String, Optional<A>> cache = new ConcurrentHashMap<>();

    private final ParentPackageAnnotationScanner<A> delegate;

    @Override
    public Optional<A> scan(AnnotatedElement element) {
        Optional<String> optionalPackageName =  getPackageName(element);

        if (optionalPackageName.isPresent()) {
            return cache.computeIfAbsent(optionalPackageName.get(), (k) -> delegate.scan(element));
        }

        return Optional.empty();

    }

    public Optional<String> getPackageName(AnnotatedElement element) {

        if (element instanceof Class) {
            return Optional.of(((Class<?>) element).getPackage().getName());
        }
        else if (element instanceof Method) {
            return Optional.of(((Method) element).getDeclaringClass().getPackage().getName());
        }

        return Optional.empty();
    }

}
