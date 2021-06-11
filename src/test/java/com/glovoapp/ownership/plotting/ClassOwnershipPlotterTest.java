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
import static java.lang.Runtime.getRuntime;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newFixedThreadPool;

import com.glovoapp.ownership.AnnotationBasedClassOwnershipExtractor;
import com.glovoapp.ownership.CachedClassOwnershipExtractor;
import com.glovoapp.ownership.classpath.ReflectionsClasspathLoader;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.plotting.extensions.html.HTMLTemplateDiagramRenderer;
import com.glovoapp.ownership.plotting.extensions.html.HTMLTemplateDiagramRenderer.Template;
import com.glovoapp.ownership.plotting.extensions.plantuml.PlantUMLDiagramRenderer;
import com.glovoapp.ownership.plotting.extensions.plantuml.PlantUMLIdentifierGenerator;
import com.glovoapp.ownership.shared.DiagramToFileDataSink;
import com.glovoapp.ownership.shared.NeighborRandomizerDiagramRendererWrapper;
import com.glovoapp.ownership.shared.UUIDIdentifierGenerator;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import org.junit.jupiter.api.Test;

@Slf4j
class ClassOwnershipPlotterTest {

    private static final String GLOVO_PACKAGE = "com.glovoapp.ownership.examples";

    private synchronized static String getDiagramsOutputDirectory() {
        final File directoryFile = new File("./target/diagrams");
        if (directoryFile.exists()) {
            if (directoryFile.isDirectory()) {
                return directoryFile.getAbsolutePath();
            } else {
                throw new IllegalStateException(directoryFile.getAbsolutePath() + " exists but is not a directory");
            }
        } else {
            if (directoryFile.mkdirs()) {
                return directoryFile.getAbsolutePath();
            } else {
                throw new IllegalStateException("unable to create " + directoryFile.getAbsolutePath());
            }
        }
    }

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteDiagramOfAllClasses() {
        ownershipPlotterWithFilter(
            isInPackageThatStartsWith(GLOVO_PACKAGE),
            getDiagramsOutputDirectory() + "/test-null-owner.svg"
        )
            .createClasspathDiagram(GLOVO_PACKAGE);
    }

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteDiagram() {
        final String desiredOwner = TEAM_A.name();
        final ClassOwnershipFilter isARelevantClassOfDesiredOwner =
            isOwnedBy(desiredOwner)
                .and(
                    hasDependenciesWithOwnerOtherThan(desiredOwner)
                        .or(hasMethodsWithOwnerOtherThan(desiredOwner))
                );
        ownershipPlotterWithFilter(
            isARelevantClassOfDesiredOwner
                .or(
                    hasMethodsOwnedBy(desiredOwner)
                        .or(
                            hasDependenciesThat(isARelevantClassOfDesiredOwner)
                        )
                )
                .or(
                    isADependencyOfAClassThat(isARelevantClassOfDesiredOwner)
                ),
            getDiagramsOutputDirectory() + "/test-team-a.svg"
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
                new UUIDIdentifierGenerator(),
                new FeaturesDiagramDataFactory(),
                new HTMLTemplateDiagramRenderer(Template.TREEMAP),
                new DiagramToFileDataSink(
                    new File(getDiagramsOutputDirectory() + '/' + desiredOwner + "_FEATURES.html")
                )
            )
        ).createClasspathDiagram(GLOVO_PACKAGE);
    }

    private static ClassOwnershipPlotter ownershipPlotterWithFilter(final ClassOwnershipFilter filter,
                                                                    final String file) {
        final int cpuCores = getRuntime().availableProcessors();
        return new ClassOwnershipPlotter(
            new ReflectionsClasspathLoader(),
            new CachedClassOwnershipExtractor(
                new AnnotationBasedClassOwnershipExtractor(
                    define(ExampleOwnershipAnnotation.class, ExampleOwnershipAnnotation::owner)
                )
            ),
            DomainOwnershipFilter.parallelized(filter, newFixedThreadPool(cpuCores), cpuCores),
            OwnershipDiagramPipeline.of(
                new PlantUMLIdentifierGenerator(),
                new RelationshipsDiagramDataFactory(),
                NeighborRandomizerDiagramRendererWrapper.wrapClassDiagram(
                    new PlantUMLDiagramRenderer(FileFormat.SVG)
                ),
                new DiagramToFileDataSink(new File(file))
            )
        );
    }

}