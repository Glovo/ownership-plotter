package com.glovoapp.ownership.plotting;

import static java.lang.System.currentTimeMillis;
import static lombok.AccessLevel.PACKAGE;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.OwnershipFilter.OwnershipContext;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface OwnershipFilter extends Predicate<OwnershipContext> {

    static OwnershipFilter any() {
        return ignore -> true;
    }

    static OwnershipFilter isInPackageThatStartsWith(final String packagePrefix) {
        return isInPackageThat(thePackage -> thePackage.getName()
                                                       .startsWith(packagePrefix))
            .named("is in package that starts with " + packagePrefix);
    }

    static OwnershipFilter isInPackageMatchingRegex(final String packageRegex) {
        return isInPackageMatchingRegex(Pattern.compile(packageRegex));
    }

    static OwnershipFilter isInPackageMatchingRegex(final Pattern packagePattern) {
        return isInPackageThat(thePackage -> packagePattern.matcher(thePackage.getName())
                                                           .matches())
            .named("is in package that matches pattern " + packagePattern);
    }

    static OwnershipFilter isInPackageThat(final Predicate<Package> packagePredicate) {
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

    static OwnershipFilter isNotOwnedBy(final String desiredOwner) {
        return isOwnedBy(desiredOwner).negate();
    }

    static OwnershipFilter isOwnedBy(final String desiredOwner) {
        return named(
            context -> Objects.equals(context.getClassOwnership()
                                             .getClassOwner(), desiredOwner),
            "is owned by " + desiredOwner
        );
    }

    static OwnershipFilter hasDependenciesOwnedBy(final String desiredOwner) {
        return hasDependenciesThat(isOwnedBy(desiredOwner))
            .named("has dependencies owned by " + desiredOwner);
    }

    static OwnershipFilter hasDependenciesWithOwnerOtherThan(final String undesiredOwner) {
        return hasDependenciesThat(isNotOwnedBy(undesiredOwner))
            .named("has dependencies with owner other than " + undesiredOwner);
    }

    static OwnershipFilter hasDependenciesThat(final OwnershipFilter dependencyFilter) {
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

    static OwnershipFilter hasMethodsOwnedBy(final String desiredOwner) {
        return hasMethodsWithOwnerThat(methodOwner -> Objects.equals(methodOwner, desiredOwner))
            .named("has methods owned by " + desiredOwner);
    }

    static OwnershipFilter hasMethodsWithOwnerOtherThan(final String undesiredOwner) {
        return hasMethodsWithOwnerThat(methodOwner -> !Objects.equals(methodOwner, undesiredOwner))
            .named("has methods with owner other than " + undesiredOwner);
    }

    static OwnershipFilter hasMethodsWithOwnerThat(final Predicate<String> ownerPredicate) {
        return named(
            context -> context.getClassOwnership()
                              .getMethodOwners()
                              .values()
                              .stream()
                              .anyMatch(ownerPredicate),
            "has methods with owner matching " + ownerPredicate
        );
    }

    static OwnershipFilter isADependencyOfAClassThat(final OwnershipFilter dependentFilter) {
        return named(
            context -> context.getDomainOwnership()
                              .stream()
                              .filter(ownership -> ownership.getDependencyOwnershipsStream()
                                                            .map(Entry::getValue)
                                                            .anyMatch(context.getClassOwnership()::equals))
                              .anyMatch(dependentOwnership ->
                                  dependentFilter.test(
                                      new OwnershipContext(dependentOwnership, context.domainOwnership)
                                  )
                              ),
            "is a dependency of a class that " + dependentFilter
        );
    }

    default OwnershipFilter negate() {
        return named(context -> !this.test(context), "not " + this);
    }

    default OwnershipFilter and(final OwnershipFilter another) {
        return composeWith(another, Boolean::logicalAnd).named("(" + this + " and " + another + ')');
    }

    default OwnershipFilter or(final OwnershipFilter another) {
        return composeWith(another, Boolean::logicalOr).named("(" + this + " or " + another + ')');
    }

    default OwnershipFilter composeWith(final OwnershipFilter another, final BinaryOperator<Boolean> operator) {
        return context -> operator.apply(this.test(context), another.test(context));
    }

    /**
     * The complexity of some filters might not be optimal when a large classpath is plotted. For example {@link
     * #isADependencyOfAClassThat(OwnershipFilter) isADependencyOfAClassThat} scans the entire {@link
     * OwnershipContext#domainOwnership domain ownership} for each {@link ClassOwnership} given. When a filter is
     * cached, it will always respond with the same result given the same {@link ClassOwnership}. The cache ignores the
     * {@link OwnershipContext#domainOwnership domain ownership} completely, making it not appropriate to use when
     * plotting multiple different domains. In some cases a cached version of given filter might be less performant than
     * non-cached version; please use {@link #debugged()} to determine whether or not the performance of this filter has
     * increased.
     *
     * @return a cached version of this filter
     */
    default OwnershipFilter cached() {
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
    default OwnershipFilter debugged() {
        final Logger log = LoggerFactory.getLogger(OwnershipFilter.class);
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

    default OwnershipFilter named(final String filterName) {
        final OwnershipFilter self = this;
        return new OwnershipFilter() {
            @Override
            public final boolean test(OwnershipContext ownershipContext) {
                return self.test(ownershipContext);
            }

            @Override
            public final String toString() {
                return filterName;
            }
        };
    }

    static OwnershipFilter named(final OwnershipFilter filter, final String filterName) {
        return filter.named(filterName);
    }

    @Getter
    @RequiredArgsConstructor(access = PACKAGE)
    final class OwnershipContext {

        private final ClassOwnership classOwnership;
        private final Collection<ClassOwnership> domainOwnership;

    }

}
