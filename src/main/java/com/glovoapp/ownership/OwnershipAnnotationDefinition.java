package com.glovoapp.ownership;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;

public interface OwnershipAnnotationDefinition {

    /**
     * @deprecated use {@link #define(Class, Function)} instead.
     */
    static OwnershipAnnotationDefinition define(@NonNull final Class<? extends Annotation> annotationClass) {
        return define(annotationClass, "owner");
    }

    /**
     * @deprecated use {@link #define(Class, Function)} instead.
     */
    @Deprecated
    @SneakyThrows
    static OwnershipAnnotationDefinition define(@NonNull final Class<? extends Annotation> annotationClass,
                                                @NonNull final String ownerGetterName) {
        final Method ownerGetterMethod = annotationClass.getDeclaredMethod(ownerGetterName);
        ownerGetterMethod.setAccessible(true);

        return define(annotationClass, annotation -> {
            try {
                return ownerGetterMethod.invoke(annotation);
            } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                throw new OwnerFetchingException(exception);
            }
        });
    }

    static <A extends Annotation> OwnershipAnnotationDefinition define(@NonNull final Class<A> annotationClass,
                                                                       @NonNull final Function<A, ?> ownerGetter) {
        return givenElement -> Optional.ofNullable(givenElement)
                                       .map(it -> {
                                           try {
                                               return it.getAnnotation(annotationClass);
                                           } catch (final Exception exception) {
                                               LoggerFactory.getLogger(OwnershipAnnotationDefinition.class)
                                                            .warn(
                                                                "failed to get annotation {} from {}, class will be ignored",
                                                                annotationClass.getSimpleName(),
                                                                it,
                                                                exception
                                                            );
                                               return null;
                                           }
                                       })
                                       .map(annotation -> {
                                           try {
                                               return String.valueOf(ownerGetter.apply(annotation));
                                           } catch (final Exception ownerGetterInvocationException) {
                                               throw new OwnerFetchingException(ownerGetterInvocationException);
                                           }
                                       });
    }

    /**
     * @param givenElement element to fetch ownership information from, be it class or a method
     * @return name of the owner or {@link Optional#empty()} if no ownership information is present
     */
    Optional<String> getOwner(final AnnotatedElement givenElement);

    /**
     * @param another a definition to be used as an alternative
     * @return a composite definition that uses another definition if this definition cannot find the owner
     */
    default OwnershipAnnotationDefinition or(final OwnershipAnnotationDefinition another) {
        return givenElement -> Optional.of(this.getOwner(givenElement))
                                       .filter(Optional::isPresent)
                                       .orElseGet(() -> another.getOwner(givenElement));
    }

    default boolean hasOwner(final AnnotatedElement givenElement) {
        return getOwner(givenElement).isPresent();
    }

    final class OwnerFetchingException extends RuntimeException {

        private OwnerFetchingException(final Throwable cause) {
            super("failed to fetch owner", cause);
        }

    }

}
