package com.glovoapp.ownership.examples;

import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_B;

@ExampleOwnershipAnnotation(owner = TEAM_A)
@AnotherExampleOwnershipAnnotation(owner = TEAM_B)
public final class ExampleClassWithBothOwnershipAnnotations {

}
