package com.glovoapp.ownership.examples.packageA;

import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_B;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ExampleOwnershipAnnotation(owner = TEAM_A)
public final class ApiA {

    private final ServiceA service;

    @ExampleOwnershipAnnotation(owner = TEAM_A)
    public final void thisClassHasALofOfMethods() {
    }

    @ExampleOwnershipAnnotation(owner = TEAM_A)
    public final void andByALotIMeanALot() {
    }

    @ExampleOwnershipAnnotation(owner = TEAM_B)
    public final void someMightEvenSeemUnnecessary() {
    }

    @ExampleOwnershipAnnotation(owner = TEAM_A)
    public final void butItIsWhatItIs() {
    }

}
