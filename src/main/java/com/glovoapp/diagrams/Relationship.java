package com.glovoapp.diagrams;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface Relationship<Id extends Identifier<Id>, Type> {

    Type getType();

    Component<Id> getSource();

    Component<Id> getDestination();

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    final class SimpleRelationship<Id extends Identifier<Id>, Type> implements Relationship<Id, Type> {

        private final Type type;
        private final Component<Id> source;
        private final Component<Id> destination;

    }

}
