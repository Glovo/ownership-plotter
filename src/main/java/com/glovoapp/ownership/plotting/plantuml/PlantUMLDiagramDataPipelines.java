package com.glovoapp.ownership.plotting.plantuml;

import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.ownership.plotting.DiagramDataPipeline;
import java.util.Collection;
import lombok.NoArgsConstructor;
import net.sourceforge.plantuml.FileFormat;

@NoArgsConstructor(access = PRIVATE)
public final class PlantUMLDiagramDataPipelines {

    public static DiagramDataPipeline pipelineForFile(final FileFormat format,
                                                      final DiagramConfiguration diagramConfiguration,
                                                      final Collection<OwnershipFilter> ownershipFilters) {
        return DiagramDataPipeline.of(
            new PlantUMLDiagramDataTransformer(diagramConfiguration, ownershipFilters),
            new PlantUMLDiagramDataFactory(format)
        );
    }

}
