package com.glovoapp.ownership.plotting.extensions.plantuml;

import com.glovoapp.diagrams.IdentifierGenerator;
import com.glovoapp.ownership.shared.UUIDIdentifierGenerator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


@EqualsAndHashCode
@RequiredArgsConstructor
public final class PlantUMLIdentifierGenerator implements IdentifierGenerator<PlantUMLIdentifier> {

    private final UUIDIdentifierGenerator delegate = new UUIDIdentifierGenerator();

    @Override
    public PlantUMLIdentifier generate(final PlantUMLIdentifier parentIdentifier, final String childComponentName) {
        return new PlantUMLIdentifier(
                delegate.generate(
                        Optional.ofNullable(parentIdentifier)
                                .map(PlantUMLIdentifier::getDelegate)
                                .orElse(null),
                        childComponentName
                )
        );
    }


}
