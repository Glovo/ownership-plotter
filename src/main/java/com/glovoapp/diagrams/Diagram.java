package com.glovoapp.diagrams;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static java.lang.System.currentTimeMillis;

public interface Diagram<Id extends Identifier<Id>, RelationshipType> {

    long getCreatedAt();

    Set<Component<Id>> getTopLevelComponents();

    Set<Relationship<Id, RelationshipType>> getRelationships();

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    final class SimpleDiagram<Id extends Identifier<Id>, RelationshipType> implements Diagram<Id, RelationshipType> {

        private final long createdAt = currentTimeMillis();

        private final Set<Component<Id>> topLevelComponents;

        private final Set<Relationship<Id, RelationshipType>> relationships;

    }

}
