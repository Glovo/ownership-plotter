package com.glovoapp.ownership.plotting;

import com.glovoapp.diagrams.DiagramDataSink;
import com.glovoapp.diagrams.DiagramRenderer;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.diagrams.IdentifierGenerator;
import com.glovoapp.ownership.ClassOwnership;
import java.util.Set;

public interface OwnershipDiagramPipeline {

    void createDiagram(final Set<ClassOwnership> domainOwnership);

    static <Id extends Identifier<Id>, RelationshipType> OwnershipDiagramPipeline of(
        final IdentifierGenerator<Id> idGenerator,
        final OwnershipDiagramFactory<RelationshipType> diagramFactory,
        final DiagramRenderer<Id, RelationshipType> renderer,
        final DiagramDataSink diagramDataSink
    ) {
        return domainOwnership -> diagramDataSink.accept(
            renderer.renderDiagram(
                diagramFactory.createOwnershipDiagram(
                    domainOwnership,
                    idGenerator
                )
            )
        );
    }

}
