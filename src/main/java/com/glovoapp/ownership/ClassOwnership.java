package com.glovoapp.ownership;

import com.glovoapp.ownership.shared.LazyReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
// We assume that fields of ClassOwnership are a pre-calculated cache and are immutable.
// Therefore two ClassOwnerships that wrap the same class are equal.
// Definitions originating from different ClassOwnershipExtractor instances should not be mixed.
// TODO: throw when comparing definitions originating from different ClassOwnershipExtractors
@EqualsAndHashCode(of = {"theClass"})
public final class ClassOwnership {

    private final Class<?> theClass;
    private final String classOwner;
    private final Map<Method, String> methodOwners;
    private final Map<Field, LazyReference<Optional<ClassOwnership>>> dependenciesOwnership;

    private static <K, V, T> Entry<K, T> mapValue(final Entry<K, V> entry, final Function<V, T> valueMapper) {
        return new SimpleEntry<>(entry.getKey(), valueMapper.apply(entry.getValue()));
    }

    public final Stream<Entry<Field, ClassOwnership>> getDependencyOwnershipsStream() {
        return dependenciesOwnership.entrySet()
                                    .stream()

                                    .map(entry -> mapValue(entry, LazyReference::get))
                                    .filter(entry -> entry.getValue()
                                                          .isPresent())
                                    .map(entry -> mapValue(entry, Optional::get));
    }

}