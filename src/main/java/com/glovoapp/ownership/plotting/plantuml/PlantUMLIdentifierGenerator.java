package com.glovoapp.ownership.plotting.plantuml;

import static java.util.UUID.randomUUID;

import com.glovoapp.diagrams.IdentifierGenerator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;


@EqualsAndHashCode
@RequiredArgsConstructor
public final class PlantUMLIdentifierGenerator implements IdentifierGenerator<PlantUMLIdentifier> {

    @Override
    public final PlantUMLIdentifier generate() {
        return new PlantUMLIdentifier(randomUUID());
    }

}
