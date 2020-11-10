package com.glovoapp.ownership.examples.packageB;

import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_B;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ExampleOwnershipAnnotation(owner = TEAM_B)
public final class ClassWithMultipleMethodsOwnedByA {

    @ExampleOwnershipAnnotation(owner = TEAM_A)
    public void oneMethodOwnedByA() {
    }

    @ExampleOwnershipAnnotation(owner = TEAM_A)
    public void anotherMethodOwnedByA() {
    }

}
