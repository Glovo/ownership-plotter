package com.glovoapp.diagrams;

import java.io.InputStream;

public interface DiagramRenderer<Id extends Identifier<Id>, RelationshipType> {

    /**
     * Transforms a given diagram specification into media, e.g. image or a 3D model.
     *
     * @param diagram the input diagram
     * @return rendered media
     */
    InputStream renderDiagram(final Diagram<Id, RelationshipType> diagram);

}
