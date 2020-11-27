package com.glovoapp.ownership.shared;

import static java.util.Collections.unmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class Sets {

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
