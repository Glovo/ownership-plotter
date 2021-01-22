package com.glovoapp.diagrams;

import java.util.Set;

public interface Diagram<Id extends Identifier<Id>, RelationshipType> {

    Set<Component<Id>> getTopLevelComponents();

    Set<Relationship<Id, RelationshipType>> getRelationships();

}
