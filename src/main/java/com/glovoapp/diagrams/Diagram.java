package com.glovoapp.diagrams;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface Diagram<Id extends Identifier<Id>, RelationshipType> {

    Set<Component<Id>> getTopLevelComponents();

    Set<Relationship<Id, RelationshipType>> getRelationships();

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    final class SimpleDiagram<Id extends Identifier<Id>, RelationshipType> implements Diagram<Id, RelationshipType> {

        private final Set<Component<Id>> topLevelComponents;

        private final Set<Relationship<Id, RelationshipType>> relationships;

    }

}
