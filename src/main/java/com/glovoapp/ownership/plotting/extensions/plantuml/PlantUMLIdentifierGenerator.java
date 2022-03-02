package com.glovoapp.ownership.plotting.extensions.plantuml;

import com.glovoapp.diagrams.IdentifierGenerator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static java.util.UUID.randomUUID;


@EqualsAndHashCode
@RequiredArgsConstructor
public final class PlantUMLIdentifierGenerator implements IdentifierGenerator<PlantUMLIdentifier> {

    @Override
    public final PlantUMLIdentifier generate() {
        return new PlantUMLIdentifier(randomUUID());
    }

}
