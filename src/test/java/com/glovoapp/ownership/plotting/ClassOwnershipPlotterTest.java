package com.glovoapp.ownership.plotting;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.define;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.hasDependenciesThat;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.hasDependenciesWithOwnerOtherThan;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.hasMetaDataElementNamed;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.hasMethodsOwnedBy;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.hasMethodsWithMetaDataElementNamed;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.hasMethodsWithOwnerOtherThan;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.isADependencyOfAClassThat;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.isInPackageThatStartsWith;
import static com.glovoapp.ownership.plotting.ClassOwnershipFilter.isOwnedBy;
import static com.glovoapp.ownership.plotting.FeaturesDiagramDataFactory.FEATURES_META_DATA_KEY;
import static com.glovoapp.ownership.plotting.FeaturesDiagramDataFactory.createFeaturesExtractor;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newFixedThreadPool;

import com.glovoapp.ownership.AnnotationBasedClassOwnershipExtractor;
import com.glovoapp.ownership.CachedClassOwnershipExtractor;
import com.glovoapp.ownership.classpath.ReflectionsClasspathLoader;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.plotting.plantuml.PlantUMLDiagramRenderer;
import com.glovoapp.ownership.plotting.plantuml.PlantUMLDiagramToFileDataSink;
import com.glovoapp.ownership.plotting.plantuml.PlantUMLIdentifierGenerator;
import java.io.File;
import net.sourceforge.plantuml.FileFormat;
import org.junit.jupiter.api.Test;

class ClassOwnershipPlotterTest {

    private static final String GLOVO_PACKAGE = "com.glovoapp";

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteDiagram() {
        ownershipPlotterWithFilter(isInPackageThatStartsWith("com.glovoapp"), "/tmp/test-null-owner.svg")
            .createClasspathDiagram(GLOVO_PACKAGE);

        final String desiredOwner = TEAM_A.name();
        final ClassOwnershipFilter isARelevantClassOfDesiredOwner = isOwnedBy(desiredOwner).and(
            hasDependenciesWithOwnerOtherThan(desiredOwner).or(
                hasMethodsWithOwnerOtherThan(desiredOwner)
            )
        );
        ownershipPlotterWithFilter(
            isARelevantClassOfDesiredOwner
                .and(
                    hasMethodsOwnedBy(desiredOwner).or(
                        hasDependenciesThat(isARelevantClassOfDesiredOwner)
                    )
                )
                .and(
                    isADependencyOfAClassThat(isARelevantClassOfDesiredOwner)
                )
                .debugged(),
            "/tmp/test-team-a.svg"
        ).createClasspathDiagram(GLOVO_PACKAGE);
    }

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteFeaturesDiagram() {
        final String desiredOwner = TEAM_A.name();
        new ClassOwnershipPlotter(
            new ReflectionsClasspathLoader(),
            new CachedClassOwnershipExtractor(
                new AnnotationBasedClassOwnershipExtractor(
                    define(
                        ExampleOwnershipAnnotation.class,
                        ExampleOwnershipAnnotation::owner,
                        singletonList(createFeaturesExtractor(ExampleOwnershipAnnotation::features))
                    )
                )
            ),
            DomainOwnershipFilter.simple(
                isOwnedBy(desiredOwner)
                    .or(hasMethodsOwnedBy(desiredOwner))
                    .and(
                        hasMetaDataElementNamed(FEATURES_META_DATA_KEY)
                            .or(hasMethodsWithMetaDataElementNamed(FEATURES_META_DATA_KEY))
                    )
            ),
            OwnershipDiagramPipeline.of(
                new PlantUMLIdentifierGenerator(),
                new FeaturesDiagramDataFactory(),
                new PlantUMLDiagramRenderer(FileFormat.SVG),
                new PlantUMLDiagramToFileDataSink(FileFormat.SVG, new File("/tmp/test-features-team-a.svg"))
            )
        ).createClasspathDiagram(GLOVO_PACKAGE);
    }

    private static ClassOwnershipPlotter ownershipPlotterWithFilter(final ClassOwnershipFilter filter,
                                                                    final String file) {
        return new ClassOwnershipPlotter(
            new ReflectionsClasspathLoader(),
            new CachedClassOwnershipExtractor(
                new AnnotationBasedClassOwnershipExtractor(
                    define(ExampleOwnershipAnnotation.class, ExampleOwnershipAnnotation::owner)
                )
            ),
            DomainOwnershipFilter.parallelized(
                filter,
                newFixedThreadPool(4),
                4
            ),
            OwnershipDiagramPipeline.of(
                new PlantUMLIdentifierGenerator(),
                new RelationshipsDiagramDataFactory(),
                new PlantUMLDiagramRenderer(FileFormat.SVG),
                new PlantUMLDiagramToFileDataSink(FileFormat.SVG, new File(file))
            )
        );
    }

}