package com.glovoapp.ownership.plotting.plantuml;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
final class Utils {

    static final Random RANDOM = new Random();
    static final int DEFAULT_INDENTATION = 2;
    static final String DEFAULT_INDENTATION_STRING = repeat(DEFAULT_INDENTATION, ' ');

    static String generateRandomId() {
        return randomUUID().toString()
                           .replace("-", "_");
    }

    static <T> Set<T> merge(final Set<T> first, final Set<T> second) {
        return Stream.concat(first.stream(), second.stream())
                     .collect(toSet());
    }

    static String repeat(final int times, final char character) {
        return IntStream.range(0, times)
                        .mapToObj(it -> character)
                        .map(String::valueOf)
                        .collect(Collectors.joining());
    }


    static String randomRepeat(final int minCount, final int maxCount, final String toRepeat) {
        final int repeatTimes = RANDOM.nextInt(maxCount - minCount) + minCount;
        return IntStream.range(0, repeatTimes)
                        .mapToObj(it -> toRepeat)
                        .collect(Collectors.joining());
    }

}
