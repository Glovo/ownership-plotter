package com.glovoapp.ownership;

import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClassOwnershipTest {

    @Test
    void equals_shouldThrow_whenExtractedWithDifferentExtractors() {
        final ClassOwnershipExtractor someExtractor = new ClassOwnershipExtractor() {
            @Override
            public final Optional<ClassOwnership> getOwnershipOf(final Class<?> theClass) {
                return Optional.of(
                    new ClassOwnership(getClass(), theClass, null, emptyMap(), emptyMap(), emptyMap(), emptyMap())
                );
            }
        };
        final ClassOwnershipExtractor someOtherExtractor = new ClassOwnershipExtractor() {
            @Override
            public final Optional<ClassOwnership> getOwnershipOf(final Class<?> theClass) {
                return Optional.of(
                    new ClassOwnership(getClass(), theClass, null, emptyMap(), emptyMap(), emptyMap(), emptyMap())
                );
            }
        };

        final Optional<ClassOwnership> someOwnership = someExtractor.getOwnershipOf(String.class);
        final Optional<ClassOwnership> someOtherOwnership = someOtherExtractor.getOwnershipOf(String.class);

        assertTrue(someOwnership.isPresent());
        assertTrue(someOtherOwnership.isPresent());
        assertThrows(IllegalArgumentException.class, () ->
            someOwnership.get()
                         .equals(someOtherOwnership.get())
        );
    }

    @Test
    void equals_shouldReturnTrue_whenExtractedWithSameExtractor() {
        final ClassOwnershipExtractor someExtractor = new ClassOwnershipExtractor() {
            @Override
            public final Optional<ClassOwnership> getOwnershipOf(final Class<?> theClass) {
                return Optional.of(
                    new ClassOwnership(getClass(), theClass, null, emptyMap(), emptyMap(), emptyMap(), emptyMap())
                );
            }
        };

        final Optional<ClassOwnership> someOwnership = someExtractor.getOwnershipOf(String.class);
        final Optional<ClassOwnership> someOtherOwnership = someExtractor.getOwnershipOf(String.class);

        assertEquals(someOwnership, someOtherOwnership);
    }

    @Test
    void equals_shouldReturnTrue_whenExtractedWithSameExtractorAndHasDifferentDataForSameClass() {
        final ClassOwnershipExtractor someExtractor = new ClassOwnershipExtractor() {
            @Override
            public final Optional<ClassOwnership> getOwnershipOf(final Class<?> theClass) {
                return Optional.of(new ClassOwnership(
                    getClass(), theClass, randomUUID().toString(), emptyMap(), emptyMap(), emptyMap(), emptyMap()
                ));
            }
        };

        final Optional<ClassOwnership> someOwnership = someExtractor.getOwnershipOf(String.class);
        final Optional<ClassOwnership> someOtherOwnership = someExtractor.getOwnershipOf(String.class);

        assertEquals(someOwnership, someOtherOwnership);
    }

}