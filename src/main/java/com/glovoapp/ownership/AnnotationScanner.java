package com.glovoapp.ownership;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public interface AnnotationScanner<A extends Annotation> {
    Optional<A> scan(AnnotatedElement element);
}
