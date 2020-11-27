package com.glovoapp.ownership.plotting;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.ClassOwnershipFilter.OwnershipContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.UnaryOperator;

public interface DomainOwnershipFilter extends UnaryOperator<Set<ClassOwnership>> {

    static DomainOwnershipFilter simple(final ClassOwnershipFilter filter) {
        return domainOwnership -> domainOwnership.stream()
                                                 .filter(ownership -> filter.test(
                                                     new OwnershipContext(ownership, domainOwnership)
                                                 ))
                                                 .collect(toSet());
    }

    /**
     * Splits given domain into smaller chunks and filters them in parallel. The separate results are collected and
     * merged once completed.
     *
     * @param executorService thread pool to be used for parallel execution
     * @param partitionsCount count of partitions the domain will be split into
     * @return a parallelized version of this {@link DomainOwnershipFilter}
     */
    default DomainOwnershipFilter parallelized(final ExecutorService executorService,
                                               final int partitionsCount) {
        if (partitionsCount < 1) {
            throw new IllegalArgumentException("partitions count must be greater than 0");
        }
        requireNonNull(executorService, "executor service must not be null");

        final DomainOwnershipFilter self = this;
        return domainOwnership -> {
            final List<Set<ClassOwnership>> partitionedOwnerships = new ArrayList<>(partitionsCount);
            for (int i = 0; i < partitionsCount; ++i) {
                partitionedOwnerships.add(new HashSet<>());
            }

            int index = 0;
            for (ClassOwnership object : domainOwnership) {
                partitionedOwnerships.get(index++ % partitionsCount)
                                     .add(object);
            }

            try {
                return executorService
                    .invokeAll(
                        partitionedOwnerships.stream()
                                             .map(ownerships ->
                                                 (Callable<Set<ClassOwnership>>) () -> self.apply(ownerships)
                                             )
                                             .collect(toList())
                    )
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (final Exception exception) {
                            throw new RuntimeException(exception);
                        }
                    })
                    .flatMap(Collection::stream)
                    .collect(toSet());
            } catch (final InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        };
    }

}
