package com.glovoapp.diagrams;

import java.io.ByteArrayInputStream;

public interface DiagramRenderer<Id extends Identifier<Id>, RelationshipType> {

    /**
     * Transforms a given diagram specification into media stream, e.g. image or a 3D model.
     *
     * @param diagram the input diagram
     * @return stream of rendered media
     */
    ByteArrayInputStream renderDiagram(final Diagram<Id, RelationshipType> diagram);

}
