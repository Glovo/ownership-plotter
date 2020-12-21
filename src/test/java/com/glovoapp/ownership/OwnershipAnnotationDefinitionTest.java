package com.glovoapp.ownership;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.define;
import static com.glovoapp.ownership.examples.ExampleOwner.EXAMPLE_OWNER;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_A;
import static com.glovoapp.ownership.examples.ExampleOwner.TEAM_B;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.glovoapp.ownership.OwnershipAnnotationDefinition.OwnershipData;
import com.glovoapp.ownership.examples.AnotherExampleOwnershipAnnotation;
import com.glovoapp.ownership.examples.ExampleClassWithAnotherOwnershipAnnotation;
import com.glovoapp.ownership.examples.ExampleClassWithBothOwnershipAnnotations;
import com.glovoapp.ownership.examples.ExampleClassWithCircularDependencies;
import com.glovoapp.ownership.examples.ExampleClassWithOwner;
import com.glovoapp.ownership.examples.ExampleClassWithoutOwner;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OwnershipAnnotationDefinitionTest {

    private final OwnershipAnnotationDefinition definition
        = define(ExampleOwnershipAnnotation.class, ExampleOwnershipAnnotation::owner);

    @SneakyThrows
    static Stream<Arguments> annotatedElementsAndExpectedOwners() {
        return Stream.of(
            arguments(ExampleClassWithOwner.class, EXAMPLE_OWNER.name()),
            arguments(ExampleClassWithOwner.class.getMethod("exampleMethodWithOwner"), EXAMPLE_OWNER.name()),
            arguments(ExampleClassWithoutOwner.class, null),
            arguments(ExampleClassWithoutOwner.class.getMethod("exampleMethodWithoutOwner"), null),
            arguments(ExampleClassWithCircularDependencies.class, EXAMPLE_OWNER.name()),
            arguments(ExampleClassWithCircularDependencies.ExampleCircularDependency.class, EXAMPLE_OWNER.name())
        );
    }

    @Test
    void or_shouldReturnADefinitionThatAllowsFetchingOwnerWithAnotherAnnotation() {
        OwnershipAnnotationDefinition newDefinition = definition.or(define(
            AnotherExampleOwnershipAnnotation.class, AnotherExampleOwnershipAnnotation::owner
        ));
        final Optional<OwnershipData> owner = newDefinition.getOwnershipData(
            ExampleClassWithAnotherOwnershipAnnotation.class);
        assertTrue(owner.isPresent());
        assertEquals(EXAMPLE_OWNER.name(), owner.get()
                                                .getOwner());
    }

    @Test
    void or_shouldReturnADefinitionThatUsesFirstAnnotationAsPriority() {
        OwnershipAnnotationDefinition anotherDefinition = define(
            AnotherExampleOwnershipAnnotation.class, AnotherExampleOwnershipAnnotation::owner
        );
        OwnershipAnnotationDefinition newDefinition = definition.or(anotherDefinition);
        Optional<OwnershipData> owner = newDefinition.getOwnershipData(ExampleClassWithBothOwnershipAnnotations.class);
        assertTrue(owner.isPresent());
        assertEquals(TEAM_A.name(), owner.get()
                                         .getOwner());

        OwnershipAnnotationDefinition reversedDefinition = anotherDefinition.or(definition);
        Optional<OwnershipData> anotherOwner = reversedDefinition.getOwnershipData(
            ExampleClassWithBothOwnershipAnnotations.class);
        assertTrue(anotherOwner.isPresent());
        assertEquals(TEAM_B.name(), anotherOwner.get()
                                                .getOwner());
    }

    @ParameterizedTest
    @MethodSource("annotatedElementsAndExpectedOwners")
    void getOwner_shouldReturnOwnerOfAClass(final AnnotatedElement element, final String expectedOwner) {
        assertEquals(
            Optional.ofNullable(expectedOwner),
            definition.getOwnershipData(element)
                      .map(OwnershipData::getOwner)
        );
    }

}