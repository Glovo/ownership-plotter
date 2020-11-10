package com.glovoapp.ownership.examples;

import com.glovoapp.ownership.examples.packageA.IrrelevantClass;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ExampleClassWithNoOwnerThatDependsOnIrrelevantClass {

    private final IrrelevantClass irrelevantClass;

}
