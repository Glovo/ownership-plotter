package com.glovoapp.ownership.plotting;

import com.glovoapp.ownership.ClassOwnership;
import java.io.OutputStream;
import java.util.Collection;

public interface DiagramDataPipeline {

    static <ClassOwnershipDiagramData> DiagramDataPipeline of(
        final DiagramDataTransformer<ClassOwnershipDiagramData> transformer,
        final DiagramFactory<ClassOwnershipDiagramData> factory
    ) {
        return (ownerPerspective, classOwnerships, outputStream) -> {
            final ClassOwnershipDiagramData diagramData = transformer.transformToDiagramData(
                ownerPerspective,
                classOwnerships
            );
            factory.generateDiagram(diagramData, outputStream);
        };
    }

    void generateDiagram(final Object ownerPerspective,
                         final Collection<ClassOwnership> classOwnerships,
                         final OutputStream outputStream);

}
