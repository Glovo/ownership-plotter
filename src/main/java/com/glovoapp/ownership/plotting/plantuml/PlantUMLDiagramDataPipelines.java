package com.glovoapp.ownership.plotting.plantuml;

import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.ownership.plotting.DiagramDataPipeline;
import lombok.NoArgsConstructor;
import net.sourceforge.plantuml.FileFormat;

@NoArgsConstructor(access = PRIVATE)
public final class PlantUMLDiagramDataPipelines {

    public static DiagramDataPipeline pipelineForFile(final FileFormat format,
                                                      final DiagramConfiguration diagramConfiguration) {
        return DiagramDataPipeline.of(
            new PlantUMLDiagramDataTransformer(diagramConfiguration),
            new PlantUMLDiagramDataFactory(format)
        );
    }

}
