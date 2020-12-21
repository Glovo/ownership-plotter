package com.glovoapp.ownership;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.define;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.glovoapp.ownership.examples.ExampleClassWithCircularDependencies;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AnnotationBasedClassOwnershipExtractorTest {

    private final AnnotationBasedClassOwnershipExtractor extractor = new AnnotationBasedClassOwnershipExtractor(
        define(ExampleOwnershipAnnotation.class, ExampleOwnershipAnnotation::owner)
    );

    @Test
    void getOwnershipOf_givenACircularDependency_shouldExtractDependencyInformation() {
        final Optional<ClassOwnership> optionalOwnership = assertDoesNotThrow(() ->
            extractor.getOwnershipOf(ExampleClassWithCircularDependencies.class)
        );
        assertTrue(optionalOwnership.isPresent());
        final ClassOwnership ownership = optionalOwnership.get();
        assertEquals(ExampleClassWithCircularDependencies.class, ownership.getTheClass());
        assertEquals(1, ownership.getDependenciesOwnership()
                                 .size());

        final Optional<ClassOwnership> optionalDependencyOwnership = ownership.getDependencyOwnershipsStream()
                                                                              .map(Entry::getValue)
                                                                              .findAny();
        assertTrue(optionalDependencyOwnership.isPresent());
        final ClassOwnership dependencyOwnership = optionalDependencyOwnership.get();
        assertEquals(
            dependencyOwnership.getTheClass(),
            ExampleClassWithCircularDependencies.ExampleCircularDependency.class
        );
        assertEquals(1, dependencyOwnership.getDependenciesOwnership()
                                           .size());

        final Optional<ClassOwnership> optionalOwnershipAgain = dependencyOwnership.getDependencyOwnershipsStream()
                                                                                   .map(Entry::getValue)
                                                                                   .findAny();
        assertTrue(optionalOwnershipAgain.isPresent());
        assertEquals(ownership, optionalOwnershipAgain.get());
    }

}