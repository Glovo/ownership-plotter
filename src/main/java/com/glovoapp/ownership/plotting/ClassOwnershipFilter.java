package com.glovoapp.ownership.plotting;

import static java.lang.System.currentTimeMillis;
import static lombok.AccessLevel.PACKAGE;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.ClassOwnershipFilter.OwnershipContext;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ClassOwnershipFilter extends Predicate<OwnershipContext> {

    static ClassOwnershipFilter isInPackageThatStartsWith(final String packagePrefix) {
        return isInPackageThat(thePackage -> thePackage.getName()
                                                       .startsWith(packagePrefix))
            .named("is in package that starts with " + packagePrefix);
    }

    static ClassOwnershipFilter isInPackageMatchingRegex(final String packageRegex) {
        return isInPackageMatchingRegex(Pattern.compile(packageRegex));
    }

    static ClassOwnershipFilter isInPackageMatchingRegex(final Pattern packagePattern) {
        return isInPackageThat(thePackage -> packagePattern.matcher(thePackage.getName())
                                                           .matches())
            .named("is in package that matches pattern " + packagePattern);
    }

    static ClassOwnershipFilter isInPackageThat(final Predicate<Package> packagePredicate) {
        return named(
            context -> Optional.of(context)
                               .map(OwnershipContext::getClassOwnership)
                               .map(ClassOwnership::getTheClass)
                               .map(Class::getPackage)
                               .map(packagePredicate::test)
                               .orElse(false),
            "is in package that matches " + packagePredicate
        );
    }

    static ClassOwnershipFilter isNotOwnedBy(final String desiredOwner) {
        return isOwnedBy(desiredOwner).negate();
    }

    static ClassOwnershipFilter isOwnedBy(final String desiredOwner) {
        return named(
            context -> Objects.equals(context.getClassOwnership()
                                             .getClassOwner(), desiredOwner),
            "is owned by " + desiredOwner
        );
    }

    static ClassOwnershipFilter hasDependenciesOwnedBy(final String desiredOwner) {
        return hasDependenciesThat(isOwnedBy(desiredOwner))
            .named("has dependencies owned by " + desiredOwner);
    }

    static ClassOwnershipFilter hasDependenciesWithOwnerOtherThan(final String undesiredOwner) {
        return hasDependenciesThat(isNotOwnedBy(undesiredOwner))
            .named("has dependencies with owner other than " + undesiredOwner);
    }

    static ClassOwnershipFilter hasDependenciesThat(final ClassOwnershipFilter dependencyFilter) {
        return named(
            context -> context.getClassOwnership()
                              .getDependencyOwnershipsStream()
                              .map(Entry::getValue)
                              .anyMatch(dependencyOwnership ->
                                  dependencyFilter.test(
                                      new OwnershipContext(dependencyOwnership, context.domainOwnership)
                                  )
                              ),
            "has dependencies that (" + dependencyFilter + ')'
        );
    }

    static ClassOwnershipFilter hasMethodsOwnedBy(final String desiredOwner) {
        return hasMethodsWithOwnerThat(methodOwner -> Objects.equals(methodOwner, desiredOwner))
            .named("has methods owned by " + desiredOwner);
    }

    static ClassOwnershipFilter hasMethodsWithOwnerOtherThan(final String undesiredOwner) {
        return hasMethodsWithOwnerThat(methodOwner -> !Objects.equals(methodOwner, undesiredOwner))
            .named("has methods with owner other than " + undesiredOwner);
    }

    static ClassOwnershipFilter hasMethodsWithOwnerThat(final Predicate<String> ownerPredicate) {
        return named(
            context -> context.getClassOwnership()
                              .getMethodOwners()
                              .values()
                              .stream()
                              .anyMatch(ownerPredicate),
            "has methods with owner matching " + ownerPredicate
        );
    }

    static ClassOwnershipFilter isADependencyOfAClassThat(final ClassOwnershipFilter dependentFilter) {
        return named(
            context -> context.getDomainOwnership()
                              .stream()
                              .filter(dependentOwnership ->
                                  dependentFilter.test(
                                      new OwnershipContext(dependentOwnership, context.domainOwnership)
                                  )
                              )
                              .anyMatch(ownership -> ownership.getDependencyOwnershipsStream()
                                                              .map(Entry::getValue)
                                                              .anyMatch(context.getClassOwnership()::equals)),
            "is a dependency of a class that " + dependentFilter
        );
    }

    default ClassOwnershipFilter negate() {
        return named(context -> !this.test(context), "not " + this);
    }

    default ClassOwnershipFilter and(final ClassOwnershipFilter another) {
        return composeWith(another, Boolean::logicalAnd).named("(" + this + " and " + another + ')');
    }

    default ClassOwnershipFilter or(final ClassOwnershipFilter another) {
        return composeWith(another, Boolean::logicalOr).named("(" + this + " or " + another + ')');
    }

    default ClassOwnershipFilter composeWith(final ClassOwnershipFilter another,
                                             final BinaryOperator<Boolean> operator) {
        return context -> operator.apply(this.test(context), another.test(context));
    }

    /**
     * The complexity of some filters might not be optimal when a large classpath is plotted. For example {@link
     * #isADependencyOfAClassThat(ClassOwnershipFilter) isADependencyOfAClassThat} scans the entire {@link
     * OwnershipContext#getDomainOwnership() domain ownership} for each {@link ClassOwnership} given. When a filter is
     * cached, it will always respond with the same result given the same {@link ClassOwnership}. The cache ignores the
     * {@link OwnershipContext#getDomainOwnership() domain ownership} completely, making it not appropriate to use when
     * plotting multiple different domains. In some cases a cached version of given filter might be less performant than
     * non-cached version; please use {@link #debugged()} to determine whether or not the performance of this filter has
     * increased.
     *
     * @return a cached version of this filter
     */
    default ClassOwnershipFilter cached() {
        final ConcurrentHashMap<ClassOwnership, Boolean> cache = new ConcurrentHashMap<>();
        return named(
            ownershipContext -> cache.computeIfAbsent(
                ownershipContext.getClassOwnership(),
                classOwnership -> this.test(ownershipContext)
            ),
            "CACHED[" + this + ']'
        );
    }

    /**
     * Adds logging for the following metrics of this filter:
     * <ul>
     *     <li>longest evaluation time</li>
     *     <li>domain filtering progress</li>
     * </ul>
     *
     * @return a debugged version of this filter
     */
    default ClassOwnershipFilter debugged() {
        final Logger log = LoggerFactory.getLogger(ClassOwnershipFilter.class);
        final AtomicInteger filteredClasses = new AtomicInteger(0);
        final AtomicInteger percentageSoFar = new AtomicInteger(0);
        final AtomicLong highestFilteringTimeMillis = new AtomicLong(0);
        return named(
            ownershipContext -> {
                final int domainOwnershipSize = ownershipContext.getDomainOwnership()
                                                                .size();

                final long startTime = currentTimeMillis();
                final boolean result = this.test(ownershipContext);
                final long endTime = currentTimeMillis();
                final long filteringTime = endTime - startTime;

                highestFilteringTimeMillis.updateAndGet(currentHighestFilteringTime -> {
                    if (filteringTime > currentHighestFilteringTime) {
                        log.info(
                            "filtering of {} in {} took longest so far: {}ms",
                            ownershipContext.getClassOwnership()
                                            .getTheClass()
                                            .getCanonicalName(),
                            this,
                            filteringTime
                        );
                        return filteringTime;
                    } else {
                        return currentHighestFilteringTime;
                    }
                });

                filteredClasses.updateAndGet(filteredClassesCount -> {
                    final int currentFilteredClassesCount = filteredClassesCount + 1;
                    final int oldPercentage = percentageSoFar.get();
                    final int newPercentage = (currentFilteredClassesCount * 100) / domainOwnershipSize;
                    percentageSoFar.set(newPercentage);
                    if (newPercentage != oldPercentage) {
                        log.info(
                            "{} filtered {}% ({}/{} classes) so far",
                            this,
                            newPercentage,
                            currentFilteredClassesCount,
                            domainOwnershipSize
                        );
                    }
                    return currentFilteredClassesCount;
                });
                return result;
            },
            "DEBUGGED[" + this + ']'
        );
    }

    default ClassOwnershipFilter named(final String filterName) {
        return named(() -> filterName);
    }

    default ClassOwnershipFilter named(final Supplier<String> filterNameSupplier) {
        final ClassOwnershipFilter self = this;
        return new ClassOwnershipFilter() {
            @Override
            public final boolean test(final OwnershipContext ownershipContext) {
                return self.test(ownershipContext);
            }

            @Override
            public final String toString() {
                return filterNameSupplier.get();
            }
        };
    }

    static ClassOwnershipFilter named(final ClassOwnershipFilter filter, final String filterName) {
        return filter.named(filterName);
    }

    @Getter
    @RequiredArgsConstructor(access = PACKAGE)
    final class OwnershipContext {

        private final ClassOwnership classOwnership;
        private final Collection<ClassOwnership> domainOwnership;

    }

}
