package com.glovoapp.diagrams;

/**
 * Helper class for generating new identifiers.
 *
 * @param <Id> the type of identifier this generator provides
 */
public interface IdentifierGenerator<Id extends Identifier<Id>> {

    /**
     * Generates a new identifier of a child component based on parent component and child component name.
     * May be used to create deterministic identifiers that are preserved between compilations.
     * Implementation of this method must be thread-safe.
     *
     * @param parentIdentifier   identifier of the parent component (null in case of top level components)
     * @param childComponentName the name of the component for which the ID is being generated (may be null)
     * @return a new identifier, unique for every unique parent/childComponentName combination
     */
    Id generate(final Id parentIdentifier, final String childComponentName);

}
