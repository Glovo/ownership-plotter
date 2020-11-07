package com.glovoapp.ownership.plotting;

import java.io.OutputStream;

public interface DiagramFactory<ClassOwnershipDiagramData> {

    void generateDiagram(final ClassOwnershipDiagramData diagramData, final OutputStream outputStream);

}
