package com.glovoapp.ownership;

import static com.glovoapp.ownership.OwnershipAnnotationDefinition.define;
import static com.glovoapp.ownership.examples.ExampleOwner.EXAMPLE_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.glovoapp.ownership.examples.ExampleClassWithCircularDependencies;
import com.glovoapp.ownership.examples.ExampleClassWithOwner;
import com.glovoapp.ownership.examples.ExampleClassWithoutOwner;
import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OwnershipAnnotationDefinitionTest {

    private final OwnershipAnnotationDefinition definition = define(ExampleOwnershipAnnotation.class);

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

    @ParameterizedTest
    @MethodSource("annotatedElementsAndExpectedOwners")
    void getOwner_shouldReturnOwnerOfAClass(final AnnotatedElement element, final String expectedOwner) {
        assertEquals(Optional.ofNullable(expectedOwner), definition.getOwner(element));
    }

}