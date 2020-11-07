package com.glovoapp.ownership;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;

public interface OwnershipAnnotationDefinition {

    static OwnershipAnnotationDefinition define(@NonNull final Class<? extends Annotation> annotationClass) {
        return define(annotationClass, "owner");
    }

    @SneakyThrows
    static OwnershipAnnotationDefinition define(@NonNull final Class<? extends Annotation> annotationClass,
                                                @NonNull final String ownerGetterName) {
        final Method ownerGetterMethod = annotationClass.getDeclaredMethod(ownerGetterName);
        ownerGetterMethod.setAccessible(true);

        return new OwnershipAnnotationDefinition() {
            @Override
            public final Optional<String> getOwner(final AnnotatedElement givenElement) {
                return Optional.ofNullable(givenElement)
                               .map(it -> it.getAnnotation(annotationClass))
                               .map(this::getOwner);
            }

            private String getOwner(@NonNull final Annotation annotation) {
                try {
                    return String.valueOf(ownerGetterMethod.invoke(annotation));
                } catch (final Exception ownerGetterInvocationException) {
                    throw new OwnerFetchingException(ownerGetterInvocationException);
                }
            }
        };
    }

    /**
     * @param givenElement element to fetch ownership information from, be it class or a method
     * @return name of the owner or {@link Optional#empty()} if no ownership information is present
     */
    Optional<String> getOwner(final AnnotatedElement givenElement);

    default boolean hasOwner(final AnnotatedElement givenElement) {
        return getOwner(givenElement).isPresent();
    }

    final class OwnerFetchingException extends RuntimeException {

        private OwnerFetchingException(final Throwable cause) {
            super("failed to fetch owner", cause);
        }

    }

}
