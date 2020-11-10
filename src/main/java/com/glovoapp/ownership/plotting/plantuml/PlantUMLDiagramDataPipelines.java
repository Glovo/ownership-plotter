package com.glovoapp.ownership.plotting.plantuml;

import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.ownership.plotting.DiagramDataPipeline;
import lombok.NoArgsConstructor;
import net.sourceforge.plantuml.FileFormat;

@NoArgsConstructor(access = PRIVATE)
public final class PlantUMLDiagramDataPipelines {

    public static DiagramDataPipeline pipelineForPNG() {
        return pipelineForFile(FileFormat.PNG);
    }

    public static DiagramDataPipeline pipelineForSVG() {
        return pipelineForFile(FileFormat.SVG);
    }

    public static DiagramDataPipeline pipelineForFile(final FileFormat format) {
        return DiagramDataPipeline.of(
            new PlantUMLDiagramDataTransformer(),
            new PlantUMLDiagramDataFactory(format)
        );
    }

}
