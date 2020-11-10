package com.glovoapp.ownership.plotting;

import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.plotting.plantuml.PlantUMLDiagramDataPipelines.pipelineForSVG;

import com.glovoapp.ownership.AnnotationBasedClassOwnershipExtractor;
import com.glovoapp.ownership.CachedClassOwnershipExtractor;
import com.glovoapp.ownership.OwnershipAnnotationDefinition;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
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

    private final ClassOwnershipPlotter classOwnershipPlotter = new ClassOwnershipPlotter(
        new CachedClassOwnershipExtractor(
            new AnnotationBasedClassOwnershipExtractor(
                OwnershipAnnotationDefinition.define(ExampleOwnershipAnnotation.class)
            )
        ),
        pipelineForSVG()
    );

    @Test
    void writeDiagramOfClassesLoadedInContextToFile_shouldWriteDiagram() {
        classOwnershipPlotter.writeDiagramOfClassesLoadedInContextToFile(null, "/tmp/test-null-owner.svg");
        classOwnershipPlotter.writeDiagramOfClassesLoadedInContextToFile(TEAM_A, "/tmp/test-team-a.svg");
    }

}