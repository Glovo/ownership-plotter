package com.glovoapp.ownership.shared;

import lombok.NoArgsConstructor;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Strings {

    public static String repeat(final int times, final char character) {
        return IntStream.range(0, times)
                        .mapToObj(it -> character)
                        .map(String::valueOf)
                        .collect(joining());
    }

}
