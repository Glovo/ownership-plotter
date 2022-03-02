package com.glovoapp.ownership.plotting.extensions.plantuml;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.ownership.shared.UUIDIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;


@EqualsAndHashCode
@RequiredArgsConstructor(access = PACKAGE)
public final class PlantUMLIdentifier implements Identifier<PlantUMLIdentifier> {

    @Getter(PACKAGE)
    private final UUIDIdentifier delegate;

    @Override
    public int compareTo(final PlantUMLIdentifier other) {
        return this.delegate.compareTo(other.delegate);
    }

    @Override
    @JsonSerialize
    public String toString() {
        // Plant UML IDs do not accept dashes but do accept underscores and alphanumeric characters.
        return delegate.toString().replace('-', '_');
    }

}
