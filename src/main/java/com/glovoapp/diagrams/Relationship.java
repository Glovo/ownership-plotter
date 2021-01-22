package com.glovoapp.diagrams;

public interface Relationship<Id extends Identifier<Id>, Type> {

    Type getType();

    Component<Id> getSource();

    Component<Id> getDestination();

}
