package com.glovoapp.ownership.shared;

import com.glovoapp.diagrams.IdentifierGenerator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;


@EqualsAndHashCode
@RequiredArgsConstructor
public final class UUIDIdentifierGenerator implements IdentifierGenerator<UUIDIdentifier> {

    @Override
    public UUIDIdentifier generate(UUIDIdentifier parentIdentifier, String childComponentName) {
        return new UUIDIdentifier(
                Optional.ofNullable(parentIdentifier)
                        .map(UUIDIdentifier::toString)
                        .map(parentUUID -> parentUUID + childComponentName)
                        .map(UUIDIdentifierGenerator::getUUIDFromString)
                        .orElseGet(() -> getUUIDFromString(childComponentName))
        );
    }

    private static UUID getUUIDFromString(final String value) {
        return UUID.nameUUIDFromBytes(("" + value).getBytes(StandardCharsets.UTF_8));
    }

}
