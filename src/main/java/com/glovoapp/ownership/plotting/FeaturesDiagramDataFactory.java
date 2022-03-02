package com.glovoapp.ownership.plotting;

import com.glovoapp.diagrams.Component.SimpleComponent;
import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.Diagram.SimpleDiagram;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.diagrams.IdentifierGenerator;
import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.OwnershipAnnotationDefinition.MetaDataExtractor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.MetaDataExtractor.metaDataExtractor;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PRIVATE;

public final class FeaturesDiagramDataFactory implements OwnershipDiagramFactory<ClassRelationship> {

    public static final String FEATURES_META_DATA_KEY = "features";

    public static <A extends Annotation, T> MetaDataExtractor<A, Set<String>> createFeaturesExtractor(
            Function<A, T[]> featuresGetter
    ) {
        return metaDataExtractor(FEATURES_META_DATA_KEY, annotation ->
                Optional.of(
                                Optional.of(annotation)
                                        .map(featuresGetter)
                                        .filter(features -> features.length != 0)
                                        .map(Arrays::stream)
                                        .orElseGet(Stream::empty)
                                        .map(String::valueOf)
                                        .collect(Collectors.toSet())
                        )
                        .filter(metaData -> !metaData.isEmpty())
        );
    }

    @Override
    public <Id extends Identifier<Id>> Diagram<Id, ClassRelationship> createOwnershipDiagram(
            final Set<ClassOwnership> domainOwnership,
            final IdentifierGenerator<Id> idGenerator
    ) {
        return new SimpleDiagram<>(
                domainOwnership.stream()
                        .flatMap(classOwnership ->
                                Stream.concat(
                                        getClassFeatures(classOwnership),
                                        getMethodFeatures(classOwnership)
                                )
                        )
                        .filter(it -> !it.getTheClass()
                                .getSimpleName()
                                .isEmpty())
                        .collect(groupingBy(
                                OwnerAndFeatureAndClassAndMethods::getOwner,
                                groupingBy(
                                        OwnerAndFeatureAndClassAndMethods::getFeature,
                                        groupingBy(
                                                OwnerAndFeatureAndClassAndMethods::getTheClass,
                                                groupingBy(
                                                        it -> it.getTheClass()
                                                                .getSimpleName(),
                                                        mapping(
                                                                OwnerAndFeatureAndClassAndMethods::getMethodNames,
                                                                toSet()
                                                        )
                                                )
                                        )
                                )
                        ))
                        .entrySet()
                        .stream()
                        .map(ownerToFeaturesToClassNamesToMethodNames -> {
                                    final Id ownerIdentifier = idGenerator.generate(null, ownerToFeaturesToClassNamesToMethodNames.getKey());
                                    return new SimpleComponent<>(
                                            ownerIdentifier,
                                            ownerToFeaturesToClassNamesToMethodNames.getKey(),
                                            ownerToFeaturesToClassNamesToMethodNames.getValue()
                                                    .entrySet()
                                                    .stream()
                                                    .map(featureToClassNameToMethodNames -> {
                                                                final Id featureIdentifier = idGenerator.generate(
                                                                        ownerIdentifier,
                                                                        featureToClassNameToMethodNames.getKey()
                                                                );
                                                                return new SimpleComponent<>(
                                                                        featureIdentifier,
                                                                        featureToClassNameToMethodNames.getKey(),
                                                                        featureToClassNameToMethodNames.getValue()
                                                                                .entrySet()
                                                                                .stream()
                                                                                .map(theClass -> {
                                                                                            final Id classIdentifier = idGenerator.generate(
                                                                                                    featureIdentifier,
                                                                                                    theClass.getKey()
                                                                                                            .getSimpleName()
                                                                                            );
                                                                                            return new SimpleComponent<>(
                                                                                                    classIdentifier,
                                                                                                    theClass.getKey()
                                                                                                            .getSimpleName(),
                                                                                                    theClass.getValue()
                                                                                                            .values()
                                                                                                            .stream()
                                                                                                            .flatMap(
                                                                                                                    Collection::stream
                                                                                                            )
                                                                                                            .flatMap(
                                                                                                                    Collection::stream
                                                                                                            )
                                                                                                            .map(methodName ->
                                                                                                                    new SimpleComponent<>(
                                                                                                                            idGenerator.generate(
                                                                                                                                    classIdentifier,
                                                                                                                                    methodName
                                                                                                                            ),
                                                                                                                            methodName,
                                                                                                                            emptySet()
                                                                                                                    ))
                                                                                                            .collect(
                                                                                                                    toSet())
                                                                                            );
                                                                                        }
                                                                                )
                                                                                .collect(
                                                                                        toSet())
                                                                );
                                                            }
                                                    )
                                                    .collect(toSet())
                                    );
                                }
                        )
                        .collect(toSet()),
                emptySet()
        );
    }

    private static Stream<OwnerAndFeatureAndClassAndMethods> getMethodFeatures(final ClassOwnership classOwnership) {
        return classOwnership.getMethodOwners()
                .entrySet()
                .stream()
                .filter(methodAndOwner -> methodAndOwner.getValue() != null)
                .flatMap(methodAndOwner ->
                        classOwnership.getMethodMetaData()
                                .entrySet()
                                .stream()
                                .filter(entry -> methodAndOwner.getKey()
                                        .equals(entry.getKey()))
                                .map(Entry::getValue)
                                .findAny()
                                .map(FeaturesDiagramDataFactory::getFeaturesFrom)
                                .map(Collection::stream)
                                .orElseGet(Stream::empty)
                                .map(feature -> new OwnerAndFeatureAndClassAndMethods(
                                        methodAndOwner.getValue(),
                                        feature,
                                        classOwnership.getTheClass(),
                                        singleton(methodAndOwner.getKey()
                                                .getName())
                                ))
                );
    }

    private static Stream<OwnerAndFeatureAndClassAndMethods> getClassFeatures(final ClassOwnership classOwnership) {
        return Optional.of(classOwnership)
                .map(ClassOwnership::getClassOwner)
                .map(owner ->
                        getFeaturesFrom(classOwnership.getMetaData())
                                .stream()
                                .map(feature -> new OwnerAndFeatureAndClassAndMethods(
                                        owner,
                                        feature,
                                        classOwnership.getTheClass(),
                                        classOwnership.getMethodMetaData()
                                                .entrySet()
                                                .stream()
                                                .filter(methodMetaData ->
                                                        getFeaturesFrom(methodMetaData.getValue())
                                                                .contains(feature)
                                                )
                                                .map(Entry::getKey)
                                                .map(Method::getName)
                                                .collect(toSet())
                                ))
                )
                .orElseGet(Stream::empty);
    }

    private static Set<String> getFeaturesFrom(final Map<String, ?> metaData) {
        return Optional.of(FEATURES_META_DATA_KEY)
                .map(metaData::get)
                .map(features -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Set<String> featuresSet = (Set<String>) features;
                        return featuresSet;
                    } catch (final ClassCastException castException) {
                        throw new IncompatibleFeaturesException(features, castException);
                    }
                })
                .orElseGet(Collections::emptySet);
    }

    @Getter(PRIVATE)
    @RequiredArgsConstructor(access = PRIVATE)
    private static final class OwnerAndFeatureAndClassAndMethods {

        private final String owner;
        private final String feature;
        private final Class<?> theClass;
        private final Set<String> methodNames;

    }

    private static final class IncompatibleFeaturesException extends RuntimeException {

        private IncompatibleFeaturesException(final Object features, final ClassCastException cause) {
            super(
                    "encountered features of incompatible type: " + features.getClass()
                            .getName()
                            + "; please make sure you are using meta-data extractors that are compatible with this diagram",
                    cause
            );
        }

    }

}
