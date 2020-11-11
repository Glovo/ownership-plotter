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

    static OwnershipFilter hasDependenciesOwnedBy(final String desiredOwner) {
        return context -> context.getClassOwnership()
                                 .getDependencyOwnershipsStream()
                                 .map(Entry::getValue)
                                 .anyMatch(dependencyOwnership ->
                                     Objects.equals(dependencyOwnership.getClassOwner(), desiredOwner)
                                 );
    }

    static OwnershipFilter hasDependenciesWithOwnerOtherThan(final String undesiredOwner) {
        return context -> context.getClassOwnership()
                                 .getDependencyOwnershipsStream()
                                 .map(Entry::getValue)
                                 .anyMatch(dependencyOwnership ->
                                     dependencyOwnership.getClassOwner() != null
                                         && !Objects.equals(dependencyOwnership.getClassOwner(), undesiredOwner)
                                 );
    }

    static OwnershipFilter hasMethodsOwnedBy(final String desiredOwner) {
//        return hasMethodsWithOwnerThat(methodOwner -> Objects.equals(methodOwner, desiredOwner));
        return context -> context.getClassOwnership()
                                 .getMethodOwners()
                                 .values()
                                 .stream()
                                 .anyMatch(methodOwners -> Objects.equals(methodOwners, desiredOwner));
    }

    static OwnershipFilter hasMethodsWithOwnerOtherThan(final String undesiredOwner) {
        return context -> context.getClassOwnership()
                                 .getMethodOwners()
                                 .values()
                                 .stream()
                                 .anyMatch(methodOwners -> !Objects.equals(methodOwners, undesiredOwner));
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
