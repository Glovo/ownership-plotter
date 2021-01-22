package com.glovoapp.diagrams;

import java.util.Set;

public interface Component<Id extends Identifier<Id>> extends Identifiable<Id> {

    String getName();

    Set<Component<Id>> getNestedComponents();

}
