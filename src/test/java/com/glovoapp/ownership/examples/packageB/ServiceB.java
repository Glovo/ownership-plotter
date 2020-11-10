package com.glovoapp.ownership.examples.packageB;

import static com.glovoapp.ownership.examples.ExampleOwner.IRRELEVANT_OWNER;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_B;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.examples.packageA.FacadeA;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ExampleOwnershipAnnotation(owner = TEAM_B)
final class ServiceB {

    private final RepositoryB repository;
    private final FacadeA facadeA;

    @ExampleOwnershipAnnotation(owner = TEAM_A)
    public final void methodInBOwnedByA() {
    }

    @ExampleOwnershipAnnotation(owner = IRRELEVANT_OWNER)
    public final void andOneMethodGoingToOtherwiseIrrelevantOwner() {
    }

}
