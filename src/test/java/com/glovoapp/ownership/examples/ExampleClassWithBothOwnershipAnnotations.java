package com.glovoapp.ownership.examples;

import static com.glovoapp.ownership.examples.ExampleFeature.FEATURE_A;
import static com.glovoapp.ownership.examples.ExampleFeature.FEATURE_B;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_B;

@ExampleOwnershipAnnotation(owner = TEAM_A, features = {FEATURE_A, FEATURE_B})
@AnotherExampleOwnershipAnnotation(owner = TEAM_B)
public final class ExampleClassWithBothOwnershipAnnotations {

}
