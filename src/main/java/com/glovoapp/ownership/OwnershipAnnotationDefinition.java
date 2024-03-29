package com.glovoapp.ownership;

import com.glovoapp.ownership.scanning.AnnotationScanner;
import com.glovoapp.ownership.scanning.CachedParentPackageAnnotationScanner;
import com.glovoapp.ownership.scanning.ParentPackageAnnotationScanner;
import com.glovoapp.ownership.shared.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

public interface OwnershipAnnotationDefinition {

    static <A extends Annotation> OwnershipAnnotationDefinition define(@NonNull final Class<A> annotationClass,
                                                                       @NonNull final Function<A, ?> ownerGetter) {
        return define(annotationClass, ownerGetter, ignore -> emptyMap());
    }

    static <A extends Annotation> OwnershipAnnotationDefinition define(@NonNull final Class<A> annotationClass,
                                                                       @NonNull final Function<A, ?> ownerGetter,
                                                                       @NonNull final List<MetaDataExtractor<A, ?>> metaDataExtractors) {
        return define(
                annotationClass,
                ownerGetter,
                annotation -> metaDataExtractors.stream()
                        .map(extractor -> Pair.of(extractor.getName(), extractor.getMetaDataGetter()
                                .apply(annotation)))
                        .filter(pair -> pair.getRight()
                                .isPresent())
                        .collect(toMap(
                                Pair::getLeft,
                                pair -> pair.getRight()
                                        .get(),
                                (firstData, secondData) -> {
                                    throw new IllegalStateException(
                                            "given two extractors for the same meta data element"
                                    );
                                }
                        ))
        );
    }

    static <A extends Annotation> OwnershipAnnotationDefinition define(@NonNull final Class<A> annotationClass,
                                                                       @NonNull final Function<A, ?> ownerGetter,
                                                                       @NonNull final Function<A, Map<String, ?>> metaDataGetter) {
        AnnotationScanner<A> parentPackageAnnotationScanner =
                new CachedParentPackageAnnotationScanner<>(
                        new ParentPackageAnnotationScanner<>(annotationClass)
                );

        return givenElement -> Optional.ofNullable(givenElement)
                .map(it -> {
                    try {
                        return Optional.of(annotationClass)
                                .map(it::getAnnotation)
                                .orElseGet(() -> annotationScanner(annotationClass, it)
                                        .orElseGet(() -> parentPackageAnnotationScanner.scan(it)
                                                .orElse(null)));
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
                        return new OwnershipData(
                                String.valueOf(ownerGetter.apply(annotation)),
                                metaDataGetter.apply(annotation)
                        );
                    } catch (final Exception ownerGetterInvocationException) {
                        throw new OwnerFetchingException(ownerGetterInvocationException);
                    }
                });
    }

    //TODO extract to class and make interface
    static <A extends Annotation> Optional<A> annotationScanner(Class<A> annotationClass, AnnotatedElement it) {
        if (it instanceof Class) {
            final Class<?> theClass = (Class<?>) it;

            //ignore lambdas and package-info
            String name = theClass.getSimpleName();
            if (name.startsWith("lambda$")
                    || name.startsWith("package-info")
            ) {
                return Optional.empty();
            }
            return Optional.ofNullable(theClass
                    .getPackage()
                    .getAnnotation(annotationClass));
        } else if (it instanceof Method) {
            final Method method = (Method) it;

            // ignore inherited methods
            if (Arrays.stream(method.getDeclaringClass()
                            .getDeclaredMethods())
                    .noneMatch(method::equals)
                    || method.getName()
                    .startsWith("$jacoco")
                    || method.getName()
                    .startsWith("lambda$")
            ) {
                return Optional.empty();
            }

            final A declaringClassAnnotation = method
                    .getDeclaringClass()
                    .getAnnotation(annotationClass);
            if (declaringClassAnnotation != null) {
                return Optional.ofNullable(declaringClassAnnotation);
            } else {
                return Optional.ofNullable(method
                        .getDeclaringClass()
                        .getPackage()
                        .getAnnotation(annotationClass));
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * @param givenElement element to fetch ownership information from, be it class or a method
     * @return ownership data or {@link Optional#empty()} if no ownership information is present
     */
    Optional<OwnershipData> getOwnershipData(final AnnotatedElement givenElement);

    /**
     * @param another a definition to be used as an alternative
     * @return a composite definition that uses another definition if this definition cannot find the owner
     */
    default OwnershipAnnotationDefinition or(final OwnershipAnnotationDefinition another) {
        return givenElement -> Optional.of(this.getOwnershipData(givenElement))
                .filter(Optional::isPresent)
                .orElseGet(() -> another.getOwnershipData(givenElement));
    }

    default boolean hasOwner(final AnnotatedElement givenElement) {
        return getOwnershipData(givenElement).isPresent();
    }

    @Getter(PRIVATE)
    @RequiredArgsConstructor(access = PRIVATE)
    final class MetaDataExtractor<A extends Annotation, T> {

        private final String name;
        private final Function<A, Optional<T>> metaDataGetter;

        public static <A extends Annotation, T> MetaDataExtractor<A, T> metaDataExtractor(
                final String key,
                final Function<A, Optional<T>> metaDataGetter
        ) {
            return new MetaDataExtractor<>(key, metaDataGetter);
        }

    }

    @Getter(PACKAGE)
    @RequiredArgsConstructor(access = PRIVATE)
    final class OwnershipData {

        private final String owner;
        private final Map<String, ?> metaData;

    }

    final class OwnerFetchingException extends RuntimeException {

        private OwnerFetchingException(final Throwable cause) {
            super("failed to fetch owner", cause);
        }

    }

}
