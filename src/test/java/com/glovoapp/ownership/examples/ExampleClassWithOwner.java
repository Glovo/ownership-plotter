package com.glovoapp.ownership.examples;

import static com.glovoapp.ownership.examples.ExampleOwner.EXAMPLE_OWNER;

@ExampleOwnershipAnnotation(owner = EXAMPLE_OWNER)
public final class ExampleClassWithOwner {

    @ExampleOwnershipAnnotation(owner = EXAMPLE_OWNER)
    public final void exampleMethodWithOwner() {
    }

}