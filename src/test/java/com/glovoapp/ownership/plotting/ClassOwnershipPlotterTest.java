package com.glovoapp.ownership.plotting;

import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.plotting.plantuml.DiagramConfiguration.defaultDiagramConfiguration;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasDependenciesOwnedBy;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasDependenciesWithOwnerOtherThan;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasMethodsOwnedBy;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasMethodsWithOwnerOtherThan;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.isADependencyOfAClassThat;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.isOwnedBy;
import static com.glovoapp.ownership.plotting.plantuml.PlantUMLDiagramDataPipelines.pipelineForFile;
import static java.util.Collections.emptyList;

import com.glovoapp.ownership.AnnotationBasedClassOwnershipExtractor;
import com.glovoapp.ownership.CachedClassOwnershipExtractor;
import com.glovoapp.ownership.OwnershipAnnotationDefinition;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.plotting.plantuml.OwnershipFilter;
import java.util.Arrays;
import java.util.Collection;
import net.sourceforge.plantuml.FileFormat;
import org.junit.jupiter.api.Test;

class ClassOwnershipPlotterTest {

    private static final String GLOVO_PACKAGE = "com.glovoapp";

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteDiagram() {
        ownershipPlotterWithNoFilters().writeDiagramOfClasspathToFile(GLOVO_PACKAGE, "/tmp/test-null-owner.svg");
        final String desiredOwner = TEAM_A.name();
        ownershipPlotterWithFilters(Arrays.asList(
            isOwnedBy(desiredOwner).and(
                hasDependenciesWithOwnerOtherThan(desiredOwner).or(
                    hasMethodsWithOwnerOtherThan(desiredOwner)
                )
            ),
            hasMethodsOwnedBy(desiredOwner).or(
                hasDependenciesOwnedBy(desiredOwner)
            ),
            isADependencyOfAClassThat(isOwnedBy(desiredOwner))
        )).writeDiagramOfClasspathToFile(GLOVO_PACKAGE, "/tmp/test-team-a.svg");
    }

    private static ClassOwnershipPlotter ownershipPlotterWithNoFilters() {
        return ownershipPlotterWithFilters(emptyList());
    }

    private static ClassOwnershipPlotter ownershipPlotterWithFilters(final Collection<OwnershipFilter> filters) {
        return new ClassOwnershipPlotter(
            new CachedClassOwnershipExtractor(
                new AnnotationBasedClassOwnershipExtractor(
                    OwnershipAnnotationDefinition.define(ExampleOwnershipAnnotation.class)
                )
            ),
            pipelineForFile(FileFormat.SVG, defaultDiagramConfiguration(), filters)
        );
    }

}