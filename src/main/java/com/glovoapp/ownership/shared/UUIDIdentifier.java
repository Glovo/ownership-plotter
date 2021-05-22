package com.glovoapp.ownership.shared;

import static lombok.AccessLevel.PACKAGE;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.glovoapp.diagrams.Identifier;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;


@EqualsAndHashCode
@RequiredArgsConstructor(access = PACKAGE)
public final class UUIDIdentifier implements Identifier<UUIDIdentifier> {

    private final UUID delegate;

    @Override
    public final int compareTo(final UUIDIdentifier other) {
        return this.delegate.compareTo(other.delegate);
    }

    @Override
    @JsonSerialize
    public final String toString() {
        return delegate.toString();
    }

}
