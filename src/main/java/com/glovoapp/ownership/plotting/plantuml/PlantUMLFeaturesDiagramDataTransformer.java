package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.MetaDataExtractor.metaDataExtractor;
import static com.glovoapp.ownership.plotting.plantuml.Utils.DEFAULT_INDENTATION_STRING;
import static com.glovoapp.ownership.plotting.plantuml.Utils.generateRandomId;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.OwnershipAnnotationDefinition.MetaDataExtractor;
import com.glovoapp.ownership.plotting.DiagramDataTransformer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.SourceStringReader;

@Slf4j
public final class PlantUMLFeaturesDiagramDataTransformer implements DiagramDataTransformer<SourceStringReader> {

    public static final String FEATURES_META_DATA_KEY = "features";

    public static <A extends Annotation, T> MetaDataExtractor<A, String[]> createFeaturesExtractor(
        Function<A, T[]> featuresGetter
    ) {
        return metaDataExtractor(FEATURES_META_DATA_KEY, annotation -> {
            final T[] features = featuresGetter.apply(annotation);
            if (features.length == 0) {
                return Optional.empty();
            }
            final String[] featureNames = new String[features.length];
            for (int i = 0; i < features.length; ++i) {
                featureNames[i] = String.valueOf(features[i]);
            }
            return Optional.of(featureNames);
        });
    }

    private final DiagramConfiguration diagramConfiguration;

    /**
     * @param diagramConfiguration global diagram settings
     */
    public PlantUMLFeaturesDiagramDataTransformer(final DiagramConfiguration diagramConfiguration) {
        this.diagramConfiguration = diagramConfiguration;
    }

    @Override
    public final SourceStringReader transformToDiagramData(final Set<ClassOwnership> domainOwnership) {
        final StringBuilder diagram = new StringBuilder("@startuml\n").append(diagramConfiguration.render());

        final Map<String, Map<String, Set<Class<?>>>> ownersByName = new HashMap<>();

        for (final ClassOwnership ownership : domainOwnership) {
            final BiConsumer<String, Set<String>> registerFeatures = (ownerName, newFeatures) -> {
                final Map<String, Set<Class<?>>> owner = ownersByName.computeIfAbsent(
                    ownerName, name -> new HashMap<>()
                );
                newFeatures.forEach(featureName -> owner.computeIfAbsent(featureName, name -> new HashSet<>())
                                                        .add(ownership.getTheClass()));
            };

            if (ownership.getClassOwner() != null && ownership.getMetaData()
                                                              .containsKey(FEATURES_META_DATA_KEY)) {
                final Set<String> classFeatures = getFeaturesFrom(ownership.getMetaData());
                registerFeatures.accept(ownership.getClassOwner(), classFeatures);
            }

            for (final Method method : ownership.getMethodOwners()
                                                .keySet()) {
                final String methodOwner = ownership.getMethodOwners()
                                                    .get(method);
                if (methodOwner != null) {
                    final Set<String> methodFeatures
                        = ownership.getMethodMetaData()
                                   .entrySet()
                                   .stream()
                                   .filter(entry -> method.equals(entry.getKey()))
                                   .map(Entry::getValue)
                                   .findAny()
                                   .map(PlantUMLFeaturesDiagramDataTransformer::getFeaturesFrom)
                                   .orElseGet(Collections::emptySet);

                    registerFeatures.accept(methodOwner, methodFeatures);
                }
            }
        }

        ownersByName.forEach((ownerName, features) -> {
            diagram.append("package ")
                   .append(ownerName)
                   .append(" as ")
                   .append(generateRandomId())
                   .append(" {\n");
            features.forEach((featureName, classes) -> {
                diagram.append(DEFAULT_INDENTATION_STRING)
                       .append("folder ")
                       .append(featureName)
                       .append(" as ")
                       .append(generateRandomId())
                       .append(" {\n");

                final List<String> classIds = new ArrayList<>();
                classes.forEach(theClass -> {
                    final String classId = generateRandomId();
                    classIds.add(classId);
                    diagram.append(DEFAULT_INDENTATION_STRING)
                           .append(DEFAULT_INDENTATION_STRING)
                           .append("rectangle ")
                           .append(theClass.getSimpleName())
                           .append(" as ")
                           .append(classId)
                           .append('\n');
                });

                if (classIds.size() >= 2) {
                    for (int i = 1; i < classIds.size(); ++i) {
                        final String previousClassId = classIds.get(i - 1);
                        final String classId = classIds.get(i);
                        diagram.append(DEFAULT_INDENTATION_STRING)
                               .append(DEFAULT_INDENTATION_STRING)
                               .append(previousClassId)
                               .append(" -[hidden]down- ")
                               .append(classId)
                               .append('\n');
                    }
                }

                diagram.append(DEFAULT_INDENTATION_STRING)
                       .append("}\n");
            });
            diagram.append("}\n");
        });

        diagram.append("@enduml\n");
        final String resultDiagram = diagram.toString();

        log.info("generated diagram {}", resultDiagram);
        return new SourceStringReader(resultDiagram);
    }

    private static Set<String> getFeaturesFrom(final Map<String, ?> metaData) {
        return Optional.of(FEATURES_META_DATA_KEY)
                       .map(metaData::get)
                       .map(String[].class::cast)
                       .map(Arrays::stream)
                       .orElseGet(Stream::empty)
                       .collect(Collectors.toSet());
    }

}
