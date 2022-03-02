package com.glovoapp.diagrams;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

public interface Component<Id extends Identifier<Id>> extends Identifiable<Id> {

    String getName();

    Set<Component<Id>> getNestedComponents();

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    final class SimpleComponent<Id extends Identifier<Id>> implements Component<Id> {

        private final Id id;
        private final String name;
        private final Set<Component<Id>> nestedComponents;

    }

}
