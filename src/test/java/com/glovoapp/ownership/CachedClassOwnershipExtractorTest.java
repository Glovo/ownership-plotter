package com.glovoapp.ownership;

import static com.glovoapp.ownership.examples.ExampleOwner.EXAMPLE_OWNER;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.glovoapp.ownership.examples.ExampleClassWithOwner;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CachedClassOwnershipExtractorTest {

    private final ClassOwnershipExtractor delegateMock = Mockito.mock(ClassOwnershipExtractor.class);
    private final CachedClassOwnershipExtractor extractor = new CachedClassOwnershipExtractor(delegateMock);

    @Test
    void getOwnershipOf_shouldReturnTheSameOwnership_forTheSameClass() {
        final Class<ExampleClassWithOwner> exampleClass = ExampleClassWithOwner.class;
        final Optional<ClassOwnership> ownership = Optional.of(
            new ClassOwnership(exampleClass, EXAMPLE_OWNER.name(), emptyMap(), emptyMap())
        );
        when(delegateMock.getOwnershipOf(exampleClass)).thenReturn(ownership);

        final Optional<ClassOwnership> firstResult = extractor.getOwnershipOf(exampleClass);

        verify(delegateMock, times(1)).getOwnershipOf(exampleClass);

        final Optional<ClassOwnership> secondResult = extractor.getOwnershipOf(exampleClass);

        verifyNoMoreInteractions(delegateMock);

        assertSame(firstResult, secondResult);
    }

}