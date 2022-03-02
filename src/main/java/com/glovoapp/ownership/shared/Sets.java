package com.glovoapp.ownership.shared;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Sets {

    /**
     * @return a new {@link Set} containing all elements of both given sets
     */
    public static <T> Set<T> union(final Set<? extends T> first, final Set<? extends T> second) {
        return Stream.concat(
                        first.stream(),
                        second.stream()
                )
                .collect(toSet());
    }

    /**
     * Splits given {@link Set} into multiple subsets.
     */
    public static <T> List<Set<T>> partition(final Set<T> set, final int numberOfPartitions) {
        if (numberOfPartitions < 1) {
            throw new IllegalArgumentException("partitions count must be greater than 0");
        }

        final List<Set<T>> result = new ArrayList<>(numberOfPartitions);
        for (int i = 0; i < numberOfPartitions; ++i) {
            result.add(new HashSet<>());
        }

        int index = 0;
        for (final T object : set) {
            result.get(index++ % numberOfPartitions)
                    .add(object);
        }

        return unmodifiableList(result);
    }

}
