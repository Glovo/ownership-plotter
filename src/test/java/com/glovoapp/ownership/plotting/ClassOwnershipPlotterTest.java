package com.glovoapp.ownership.plotting;

import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.plotting.plantuml.DiagramConfiguration.defaultDiagramConfiguration;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasDependenciesOwnedBy;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasDependenciesWithOwnerOtherThan;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasMethodsOwnedBy;
import static com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.hasMethodsWithOwnerOtherThan;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

class ClassOwnershipPlotterTest {

    @BeforeAll
    static void loadAllGlovoClasses() {
        // Load all classes in "com.glovoapp" package for analysis.
        // We need to do this because class loaders are lazy and wouldn't load classes unless explicitly asked for them.
        Reflections reflections = new Reflections("com.glovoapp", new SubTypesScanner(false));
        reflections.getSubTypesOf(Object.class);
    }

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteDiagram() {
        ownershipPlotterWithNoFilters().writeDiagramOfClassesLoadedInContextToFile("/tmp/test-null-owner.svg");
        final String desiredOwner = TEAM_A.name();
        ownershipPlotterWithFilters(Arrays.asList(
            isOwnedBy(desiredOwner).and(
                hasDependenciesWithOwnerOtherThan(desiredOwner).or(
                    hasMethodsWithOwnerOtherThan(desiredOwner)
                )
            ),
            hasMethodsOwnedBy(desiredOwner).and(
                hasDependenciesOwnedBy(desiredOwner)
            )
        )).writeDiagramOfClassesLoadedInContextToFile("/tmp/test-team-a.svg");
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