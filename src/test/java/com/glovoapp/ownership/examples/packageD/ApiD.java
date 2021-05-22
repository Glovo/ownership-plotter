package com.glovoapp.ownership.examples.packageD;

import static com.glovoapp.ownership.examples.ExampleFeature.FEATURE_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;

public final class ApiD {

    public final void thisMethodShouldBeImplicitlyOwnedByDBasedOnPackage() {
    }

    @ExampleOwnershipAnnotation(owner = TEAM_A, features = FEATURE_A)
    public final void thisMethodIsOverriddenAsOwnedByTeamA() {
    }

}
