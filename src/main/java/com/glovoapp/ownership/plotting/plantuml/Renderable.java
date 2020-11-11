package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Utils.DEFAULT_INDENTATION;
import static com.glovoapp.ownership.plotting.plantuml.Utils.repeat;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;

interface Renderable {

    /**
     * @return a line that can be rendered by PlantUML, ending with new line
     */
    String render();

    default String renderWithDefaultIndent() {
        return renderIndented(DEFAULT_INDENTATION);
    }

    default String renderIndented(final int indentation) {
        final String indentationString = repeat(indentation, ' ');
        final String render = render();
        return Arrays.stream(render.split("\n"))
                     .map(indentationString::concat)
                     .collect(joining("\n"))
            + (render.endsWith("\n") ? '\n' : "");
    }

}
