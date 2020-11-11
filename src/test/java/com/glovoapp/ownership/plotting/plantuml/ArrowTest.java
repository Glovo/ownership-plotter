package com.glovoapp.ownership.plotting.plantuml;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.glovoapp.ownership.plotting.plantuml.Arrow.Attribute;
import com.glovoapp.ownership.plotting.plantuml.Arrow.Direction;
import com.glovoapp.ownership.plotting.plantuml.Arrow.HeadStyle;
import com.glovoapp.ownership.plotting.plantuml.Arrow.LineStyle;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ArrowTest {

    @ParameterizedTest
    @MethodSource("arrowAndExpectedRendering")
    void render_shouldRenderExpectedString(final Arrow arrow, final String expectedRender) {
        final String actualRender = arrow.render();
        assertEquals(expectedRender, actualRender);
    }

    static Stream<Arguments> arrowAndExpectedRendering() {
        return Stream.of(
            arguments(
                Arrow.builder()
                     .build(),
                "-[#000000]->"
            ),
            arguments(
                Arrow.builder()
                     .direction(Direction.BOTH_WAYS)
                     .headStyle(HeadStyle.DIAMOND)
                     .color(new Color(250, 128, 32))
                     .lineStyle(LineStyle.DASHED)
                     .length(5)
                     .attributes(singleton(Attribute.DASHED))
                     .build(),
                "*=[#FA8020,dashed]=====*"
            ),
            arguments(
                Arrow.builder()
                     .direction(Direction.RIGHT_TO_LEFT)
                     .attributes(Stream.of(Attribute.BOLD, Attribute.DASHED)
                                       .collect(toSet()))
                     .length(0)
                     .build(),
                "<-[#000000,dashed,bold]"
            )
        );
    }

}