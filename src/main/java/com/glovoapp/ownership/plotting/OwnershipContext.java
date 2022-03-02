package com.glovoapp.ownership.plotting;

import com.glovoapp.ownership.ClassOwnership;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static lombok.AccessLevel.PACKAGE;

@Getter
@RequiredArgsConstructor(access = PACKAGE)
public final class OwnershipContext {

    private final ClassOwnership classOwnership;
    private final Collection<ClassOwnership> domainOwnership;

}