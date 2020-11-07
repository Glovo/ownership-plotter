package com.glovoapp.ownership.examples;

import static com.glovoapp.ownership.examples.ExampleOwner.EXAMPLE_OWNER;


@ExampleOwnershipAnnotation(owner = EXAMPLE_OWNER)
public final class ExampleClassWithCircularDependencies {

    private ExampleCircularDependency dependency;

    @ExampleOwnershipAnnotation(owner = EXAMPLE_OWNER)
    public static final class ExampleCircularDependency {

        private ExampleClassWithCircularDependencies dependency;

    }

}
