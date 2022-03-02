package com.glovoapp.ownership;

import com.glovoapp.ownership.OwnershipAnnotationDefinition.OwnershipData;
import com.glovoapp.ownership.shared.LazyReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.glovoapp.ownership.shared.Maps.transformValues;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
@RequiredArgsConstructor
public final class AnnotationBasedClassOwnershipExtractor implements ClassOwnershipExtractor {

    private final OwnershipAnnotationDefinition ownershipAnnotationDefinition;

    @Override
    public final Optional<ClassOwnership> getOwnershipOf(final Class<?> aClass) {
        try {
            final Optional<OwnershipData> classOwner = ownershipAnnotationDefinition.getOwnershipData(aClass);

            final Map<Method, OwnershipData> methodOwners
                    = Arrays.stream(aClass.getDeclaredMethods())
                    .filter(ownershipAnnotationDefinition::hasOwner)
                    .collect(toMap(
                            identity(),
                            method -> ownershipAnnotationDefinition.getOwnershipData(method)
                                    .orElseThrow(() -> new IllegalStateException(
                                            "this should never happen"
                                    ))));

            final Map<Field, LazyReference<Optional<ClassOwnership>>> dependenciesOwnership
                    = Arrays.stream(aClass.getDeclaredFields())
                    .collect(toMap(
                            field -> field,
                            field -> new LazyReference<>(() -> getOwnershipOf(field.getType()))
                    ));

            return Optional.of(new ClassOwnership(
                    getClass(),
                    aClass,
                    classOwner.map(OwnershipData::getOwner)
                            .orElse(null),
                    classOwner.map(OwnershipData::getMetaData)
                            .orElseGet(Collections::emptyMap),
                    transformValues(methodOwners, OwnershipData::getOwner),
                    transformValues(methodOwners, OwnershipData::getMetaData),
                    dependenciesOwnership
            ));
        } catch (final NoClassDefFoundError error) {
            log.info("failed to fetch ownership of {}", aClass);
            return Optional.empty();
        }
    }

}
