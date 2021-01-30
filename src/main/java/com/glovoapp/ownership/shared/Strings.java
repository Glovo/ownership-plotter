package com.glovoapp.ownership.shared;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.util.stream.IntStream;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Strings {

    public static String repeat(final int times, final char character) {
        return IntStream.range(0, times)
                        .mapToObj(it -> character)
                        .map(String::valueOf)
                        .collect(joining());
    }

}
