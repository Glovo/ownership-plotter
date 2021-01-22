package com.glovoapp.diagrams;

/**
 * Helper class for generating new identifiers.
 *
 * @param <Id> the type of identifier this generator provides
 */
public interface IdentifierGenerator<Id extends Identifier<Id>> {

    /**
     * Implementation of this method must be thread-safe.
     *
     * @return a new, unique identifier
     */
    Id generate();

}
