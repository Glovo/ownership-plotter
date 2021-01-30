package com.glovoapp.ownership.plotting;

import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.diagrams.IdentifierGenerator;
import com.glovoapp.ownership.ClassOwnership;
import java.util.Set;

public interface OwnershipDiagramFactory<RelationshipType> {

    <Id extends Identifier<Id>> Diagram<Id, RelationshipType> createOwnershipDiagram(
        final Set<ClassOwnership> domainOwnership,
        final IdentifierGenerator<Id> idGenerator
    );

}
