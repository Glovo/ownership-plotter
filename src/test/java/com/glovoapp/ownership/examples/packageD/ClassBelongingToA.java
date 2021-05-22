package com.glovoapp.ownership.examples.packageD;

import static com.glovoapp.ownership.examples.ExampleFeature.FEATURE_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;

@ExampleOwnershipAnnotation(owner = TEAM_A, features = FEATURE_A)
public final class ClassBelongingToA {

    public final void thisMethodShouldBeImplicitlyOwnedByDBasedOnClass() {
    }

}
