package com.glovoapp.ownership;

import java.util.Optional;

public interface ClassOwnershipExtractor {

    Optional<ClassOwnership> getOwnershipOf(final Class<?> aClass);

}
