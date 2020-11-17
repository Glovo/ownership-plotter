package com.glovoapp.ownership.examples.packageA;

import static com.glovoapp.ownership.examples.ExampleOwner.OWNER_WITH_ONLY_METHODS;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;

import com.glovoapp.ownership.examples.ExampleClassWithoutOwnershipThatIsADependencyOfAClassWithOwnership;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ExampleOwnershipAnnotation(owner = TEAM_A)
public final class FacadeA {

    private final ServiceA service;
    private final ExampleClassWithoutOwnershipThatIsADependencyOfAClassWithOwnership test;

    @ExampleOwnershipAnnotation(owner = OWNER_WITH_ONLY_METHODS)
    public void methodOwnedByOwnerWithOnlyMethods() {
    }

}
