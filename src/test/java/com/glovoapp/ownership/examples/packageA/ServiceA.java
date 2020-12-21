package com.glovoapp.ownership.examples.packageA;

import static com.glovoapp.ownership.examples.ExampleFeature.FEATURE_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_B;

import com.glovoapp.ownership.examples.ExampleFeature;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.examples.packageB.FacadeB;
import com.glovoapp.ownership.examples.packageC.SomeClassC;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ExampleOwnershipAnnotation(owner = TEAM_A, features = FEATURE_A)
final class ServiceA {

    private final RepositoryA repository;
    private final FacadeB facadeB;
    private final SomeClassC someClassC;

    @ExampleOwnershipAnnotation(owner = TEAM_B)
    public final void methodInAOwnedByB() {
    }

}
