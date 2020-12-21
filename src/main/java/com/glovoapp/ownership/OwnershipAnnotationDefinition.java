package com.glovoapp.ownership;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.ownership.shared.Pair;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;

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
                                               return new OwnershipData(
                                                   String.valueOf(ownerGetter.apply(annotation)),
                                                   metaDataGetter.apply(annotation)
                                               );
                                           } catch (final Exception ownerGetterInvocationException) {
                                               throw new OwnerFetchingException(ownerGetterInvocationException);
                                           }
                                       });
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
