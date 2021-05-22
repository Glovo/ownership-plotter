package com.glovoapp.ownership.shared;

import static java.util.UUID.randomUUID;

import com.glovoapp.diagrams.IdentifierGenerator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;


@EqualsAndHashCode
@RequiredArgsConstructor
public final class UUIDIdentifierGenerator implements IdentifierGenerator<UUIDIdentifier> {

    @Override
    public final UUIDIdentifier generate() {
        return new UUIDIdentifier(randomUUID());
    }

}
