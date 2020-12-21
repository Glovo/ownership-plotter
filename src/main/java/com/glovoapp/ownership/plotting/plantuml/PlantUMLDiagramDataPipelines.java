package com.glovoapp.ownership.plotting.plantuml;

import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.ownership.plotting.DiagramDataPipeline;
import lombok.NoArgsConstructor;
import net.sourceforge.plantuml.FileFormat;

@NoArgsConstructor(access = PRIVATE)
public final class PlantUMLDiagramDataPipelines {

    public static DiagramDataPipeline relationshipsPipelineForFile(final FileFormat format,
                                                                   final DiagramConfiguration diagramConfiguration) {
        return DiagramDataPipeline.of(
            new PlantUMLRelationshipsDiagramDataTransformer(diagramConfiguration),
            new PlantUMLDiagramDataFactory(format)
        );
    }

    public static DiagramDataPipeline featuresPipelineForFile(final FileFormat format,
                                                              final DiagramConfiguration diagramConfiguration) {
        return DiagramDataPipeline.of(
            new PlantUMLFeaturesDiagramDataTransformer(diagramConfiguration),
            new PlantUMLDiagramDataFactory(format)
        );
    }

}
