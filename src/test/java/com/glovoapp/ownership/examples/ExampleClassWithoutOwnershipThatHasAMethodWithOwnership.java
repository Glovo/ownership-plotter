package com.glovoapp.ownership.examples;

import static com.glovoapp.ownership.examples.ExampleOwner.EXAMPLE_OWNER;

public final class ExampleClassWithoutOwnershipThatHasAMethodWithOwnership {

    @ExampleOwnershipAnnotation(owner = EXAMPLE_OWNER)
    public final void methodWithOwnership() {
    }

}
