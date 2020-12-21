package com.glovoapp.ownership;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import com.glovoapp.ownership.OwnershipAnnotationDefinition.OwnershipData;
import com.glovoapp.ownership.shared.LazyReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class AnnotationBasedClassOwnershipExtractor implements ClassOwnershipExtractor {

    private final OwnershipAnnotationDefinition ownershipAnnotationDefinition;

    @Override
    public final Optional<ClassOwnership> getOwnershipOf(final Class<?> aClass) {
        try {
            final String classOwner = ownershipAnnotationDefinition.getOwnershipData(aClass)
                                                                   .map(OwnershipData::getOwner)
                                                                   .orElse(null);

            final Map<Method, String> methodOwners
                = Arrays.stream(aClass.getDeclaredMethods())
                        .filter(ownershipAnnotationDefinition::hasOwner)
                        .collect(toMap(
                            method -> method,
                            method -> ownershipAnnotationDefinition.getOwnershipData(method)
                                                                   .map(OwnershipData::getOwner)
                                                                   .orElseThrow(() ->
                                                                       new RuntimeException("this should never happen")
                                                                   )
                        ));

            final Map<Field, LazyReference<Optional<ClassOwnership>>> dependenciesOwnership
                = Arrays.stream(aClass.getDeclaredFields())
                        .collect(toMap(
                            field -> field,
                            field -> new LazyReference<>(() -> getOwnershipOf(field.getType()))
                        ));

            return Optional.of(new ClassOwnership(
                getClass(),
                aClass,
                classOwner,
                emptyMap(),
                methodOwners,
                emptyMap(),
                dependenciesOwnership
            ));
        } catch (final NoClassDefFoundError error) {
            log.info("failed to fetch ownership of {}", aClass);
            return Optional.empty();
        }
    }

}
