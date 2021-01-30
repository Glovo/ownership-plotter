package com.glovoapp.ownership.plotting;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.MetaDataExtractor.metaDataExtractor;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.diagrams.Component.SimpleComponent;
import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.Diagram.SimpleDiagram;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.diagrams.IdentifierGenerator;
import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.OwnershipAnnotationDefinition.MetaDataExtractor;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    public final <Id extends Identifier<Id>> Diagram<Id, ClassRelationship> createOwnershipDiagram(
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
                           .collect(groupingBy(
                               OwnerAndFeatureAndClass::getOwner,
                               groupingBy(
                                   OwnerAndFeatureAndClass::getFeature,
                                   mapping(
                                       OwnerAndFeatureAndClass::getTheClass,
                                       mapping(
                                           Class::getSimpleName,
                                           toSet()
                                       )
                                   )
                               )
                           ))
                           .entrySet()
                           .stream()
                           .map(ownerToFeaturesToClassNames ->
                               new SimpleComponent<>(
                                   idGenerator.generate(),
                                   ownerToFeaturesToClassNames.getKey(),
                                   ownerToFeaturesToClassNames.getValue()
                                                              .entrySet()
                                                              .stream()
                                                              .map(featureToClassNames ->
                                                                  new SimpleComponent<>(
                                                                      idGenerator.generate(),
                                                                      featureToClassNames.getKey(),
                                                                      featureToClassNames.getValue()
                                                                                         .stream()
                                                                                         .map(className ->
                                                                                             new SimpleComponent<>(
                                                                                                 idGenerator.generate(),
                                                                                                 className,
                                                                                                 emptySet()
                                                                                             )
                                                                                         )
                                                                                         .collect(toSet())
                                                                  )
                                                              )
                                                              .collect(toSet())
                               )
                           )
                           .collect(toSet()),
            emptySet()
        );
    }

    private static Stream<OwnerAndFeatureAndClass> getMethodFeatures(final ClassOwnership classOwnership) {
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
                                               .map(feature -> new OwnerAndFeatureAndClass(
                                                   methodAndOwner.getValue(),
                                                   feature,
                                                   classOwnership.getTheClass()
                                               ))
                             );
    }

    private static Stream<OwnerAndFeatureAndClass> getClassFeatures(final ClassOwnership classOwnership) {
        return Optional.of(classOwnership)
                       .map(ClassOwnership::getClassOwner)
                       .map(owner ->
                           getFeaturesFrom(classOwnership.getMetaData())
                               .stream()
                               .map(feature -> new OwnerAndFeatureAndClass(
                                   owner,
                                   feature,
                                   classOwnership.getTheClass()
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
    private static final class OwnerAndFeatureAndClass {

        private final String owner;
        private final String feature;
        private final Class<?> theClass;

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
