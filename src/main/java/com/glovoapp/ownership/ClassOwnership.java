package com.glovoapp.ownership;

import com.glovoapp.ownership.shared.LazyReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * We assume that fields of ClassOwnership are a pre-calculated cache and are immutable. Therefore two {@link
 * ClassOwnership ClassOwnerships} that wrap the same class are equal. Definitions originating from different {@link
 * ClassOwnershipExtractor} instances should not be mixed, as this will result in exceptions thrown from the {@link
 * #equals(Object) equals} method.
 */
@Getter
@RequiredArgsConstructor
public final class ClassOwnership {

    private final Class<? extends ClassOwnershipExtractor> extractorClass;
    private final Class<?> theClass;
    private final String classOwner;
    private final Map<String, ?> metaData;
    private final Map<Method, String> methodOwners;
    private final Map<Method, Map<String, ?>> methodMetaData;
    private final Map<Field, LazyReference<Optional<ClassOwnership>>> dependenciesOwnership;

    public final Stream<Entry<Field, ClassOwnership>> getDependencyOwnershipsStream() {
        return dependenciesOwnership.entrySet()
                .stream()

                .map(entry -> mapValue(entry, LazyReference::get))
                .filter(entry -> entry.getValue()
                        .isPresent())
                .map(entry -> mapValue(entry, Optional::get));
    }

    @Override
    public final boolean equals(final Object anotherObject) {
        if (this == anotherObject) {
            return true;
        } else if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }

        final ClassOwnership other = (ClassOwnership) anotherObject;

        if (!Objects.equals(this.extractorClass, other.extractorClass)) {
            throw new IllegalArgumentException(
                    "mixing ownerships created with different extractors: "
                            + this.extractorClass.getSimpleName()
                            + " and " + other.extractorClass.getSimpleName()
            );
        }

        return Objects.equals(this.theClass, other.theClass);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(theClass);
    }

    private static <K, V, T> Entry<K, T> mapValue(final Entry<K, V> entry, final Function<V, T> valueMapper) {
        return new SimpleEntry<>(entry.getKey(), valueMapper.apply(entry.getValue()));
    }

}