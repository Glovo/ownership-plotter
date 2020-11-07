package com.glovoapp.ownership.examples.packageA;

import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ExampleOwnershipAnnotation(owner = TEAM_A)
public final class ApiA {

    private final ServiceA service;

}
