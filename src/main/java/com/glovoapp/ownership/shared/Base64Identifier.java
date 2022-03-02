package com.glovoapp.ownership.shared;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.glovoapp.diagrams.Identifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PACKAGE)
public final class Base64Identifier implements Identifier<Base64Identifier> {

    @Getter(PACKAGE)
    private final String value;

    @Override
    public int compareTo(final Base64Identifier other) {
        return this.value.compareTo(other.value);
    }

    @Override
    @JsonSerialize
    public String toString() {
        return value;
    }

}
