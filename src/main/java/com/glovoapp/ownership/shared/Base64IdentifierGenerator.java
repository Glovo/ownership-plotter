package com.glovoapp.ownership.shared;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Generates deterministic IDs in form of Base64-encoded component names.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Base64IdentifierGenerator implements com.glovoapp.diagrams.IdentifierGenerator<Base64Identifier> {

    private static final char SEPARATOR = '.';
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Base64.Encoder ENCODER = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    @Override
    public Base64Identifier generate(final Base64Identifier parentIdentifier, final String childComponentName) {
        return encode(
                Optional.ofNullable(parentIdentifier)
                        .map(Base64IdentifierGenerator::decode)
                        .map(parentIdDecoded -> parentIdDecoded + SEPARATOR + childComponentName)
                        .orElse(childComponentName)
        );
    }

    private static Base64Identifier encode(final String value) {
        return new Base64Identifier(ENCODER.encodeToString(("" + value).getBytes(CHARSET)));
    }

    private static String decode(final Base64Identifier identifier) {
        return new String(DECODER.decode(identifier.getValue()), CHARSET);
    }

}
