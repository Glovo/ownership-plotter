package com.glovoapp.ownership.plotting;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.define;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.plotting.plantuml.DiagramConfiguration.defaultDiagramConfiguration;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasDependenciesThat;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasDependenciesWithOwnerOtherThan;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasMethodsOwnedBy;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasMethodsWithOwnerOtherThan;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.isADependencyOfAClassThat;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.isInPackageThatStartsWith;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.isOwnedBy;
import static com.glovoapp.ownership.plotting.plantuml.PlantUMLDiagramDataPipelines.pipelineForFile;
import static java.util.Arrays.asList;

import com.glovoapp.ownership.AnnotationBasedClassOwnershipExtractor;
import com.glovoapp.ownership.CachedClassOwnershipExtractor;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.plotting.plantuml.OwnershipFilter;
import net.sourceforge.plantuml.FileFormat;
import org.junit.jupiter.api.Test;

class ClassOwnershipPlotterTest {

    private static final String GLOVO_PACKAGE = "com.glovoapp";

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteDiagram() {
        ownershipPlotterWithFilters(isInPackageThatStartsWith("com.glovoapp"))
            .writeDiagramOfClasspathToFile(GLOVO_PACKAGE, "/tmp/test-null-owner.svg");

        final String desiredOwner = TEAM_A.name();
        final OwnershipFilter isARelevantClassOfDesiredOwner = isOwnedBy(desiredOwner).and(
            hasDependenciesWithOwnerOtherThan(desiredOwner).or(
                hasMethodsWithOwnerOtherThan(desiredOwner)
            )
        );
        ownershipPlotterWithFilters(
            isARelevantClassOfDesiredOwner,
            hasMethodsOwnedBy(desiredOwner).or(
                hasDependenciesThat(isARelevantClassOfDesiredOwner)
            ),
            isADependencyOfAClassThat(isARelevantClassOfDesiredOwner)
        ).writeDiagramOfClasspathToFile(GLOVO_PACKAGE, "/tmp/test-team-a.svg");
    }

    private static ClassOwnershipPlotter ownershipPlotterWithFilters(final OwnershipFilter... filters) {
        return new ClassOwnershipPlotter(
            new CachedClassOwnershipExtractor(
                new AnnotationBasedClassOwnershipExtractor(
                    define(ExampleOwnershipAnnotation.class, ExampleOwnershipAnnotation::owner)
                )
            ),
            pipelineForFile(FileFormat.SVG, defaultDiagramConfiguration(), asList(filters))
        );
    }

}