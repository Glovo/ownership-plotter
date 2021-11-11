package com.glovoapp.ownership.scanning;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public interface AnnotationScanner<A extends Annotation> {
    Optional<A> scan(final AnnotatedElement element);
}
