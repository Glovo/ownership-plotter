package com.glovoapp.ownership.plotting;

import static lombok.AccessLevel.PACKAGE;

import com.glovoapp.ownership.ClassOwnership;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = PACKAGE)
final class OwnershipContext {

    private final ClassOwnership classOwnership;
    private final Collection<ClassOwnership> domainOwnership;

}