package com.glovoapp.ownership.plotting.plantuml;

import static lombok.AccessLevel.PACKAGE;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.glovoapp.diagrams.Identifier;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;


@EqualsAndHashCode
@RequiredArgsConstructor(access = PACKAGE)
public final class PlantUMLIdentifier implements Identifier<PlantUMLIdentifier> {

    private final UUID delegate;

    @Override
    public final int compareTo(final PlantUMLIdentifier other) {
        return this.delegate.compareTo(other.delegate);
    }

    @Override
    @JsonSerialize
    public final String toString() {
        // Plant UML IDs do not accept dashes but do accept underscores and alphanumeric characters.
        return delegate.toString()
                       .replace('-', '_');
    }

}
