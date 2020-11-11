package com.glovoapp.ownership.plotting.plantuml;

import static lombok.AccessLevel.PACKAGE;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.plotting.plantuml.OwnershipFilter.OwnershipContext;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface OwnershipFilter extends Predicate<OwnershipContext> {

    static OwnershipFilter isOwnedBy(final String desiredOwner) {
        return context -> Objects.equals(context.getClassOwnership()
                                                .getClassOwner(), desiredOwner);
    }

    static OwnershipFilter isNotOwnedBy(final String desiredOwner) {
        return isOwnedBy(desiredOwner).negate();
    }

    static OwnershipFilter hasDependenciesOwnedBy(final String desiredOwner) {
        return hasDependenciesThat(isOwnedBy(desiredOwner));
    }

    static OwnershipFilter hasDependenciesWithOwnerOtherThan(final String undesiredOwner) {
        return hasDependenciesThat(isNotOwnedBy(undesiredOwner));
    }

    static OwnershipFilter hasDependenciesThat(final OwnershipFilter dependencyFilter) {
        return context -> context.getClassOwnership()
                                 .getDependencyOwnershipsStream()
                                 .map(Entry::getValue)
                                 .anyMatch(dependencyOwnership ->
                                     dependencyFilter.test(
                                         new OwnershipContext(dependencyOwnership, context.domainOwnership)
                                     )
                                 );
    }

    static OwnershipFilter hasMethodsOwnedBy(final String desiredOwner) {
        return hasMethodsWithOwnerThat(methodOwner -> Objects.equals(methodOwner, desiredOwner));
    }

    static OwnershipFilter hasMethodsWithOwnerOtherThan(final String undesiredOwner) {
        return hasMethodsWithOwnerThat(methodOwner -> !Objects.equals(methodOwner, undesiredOwner));
    }

    static OwnershipFilter hasMethodsWithOwnerThat(final Predicate<String> ownerPredicate) {
        return context -> context.getClassOwnership()
                                 .getMethodOwners()
                                 .values()
                                 .stream()
                                 .anyMatch(ownerPredicate);
    }

    default OwnershipFilter negate() {
        return context -> !this.test(context);
    }

    default OwnershipFilter and(final OwnershipFilter another) {
        return composeWith(another, Boolean::logicalAnd);
    }

    default OwnershipFilter or(final OwnershipFilter another) {
        return composeWith(another, Boolean::logicalOr);
    }

    default OwnershipFilter composeWith(final OwnershipFilter another, final BinaryOperator<Boolean> operator) {
        return context -> operator.apply(this.test(context), another.test(context));
    }

    @Getter
    @RequiredArgsConstructor(access = PACKAGE)
    final class OwnershipContext {

        private final ClassOwnership classOwnership;
        private final Collection<ClassOwnership> domainOwnership;

    }

}
