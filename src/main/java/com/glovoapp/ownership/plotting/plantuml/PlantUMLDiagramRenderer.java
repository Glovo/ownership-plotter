package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Color.randomReadableColor;
import static com.glovoapp.ownership.shared.Strings.repeat;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import com.glovoapp.diagrams.Component;
import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.DiagramRenderer;
import com.glovoapp.diagrams.Relationship;
import com.glovoapp.ownership.plotting.ClassRelationship;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

@Slf4j
@RequiredArgsConstructor
public final class PlantUMLDiagramRenderer implements DiagramRenderer<PlantUMLIdentifier, ClassRelationship> {

    private final Random random = new Random();
    private final FileFormat fileFormat;

    @Override
    @SneakyThrows
    public final InputStream renderDiagram(final Diagram<PlantUMLIdentifier, ClassRelationship> diagram) {
        final String diagramAsString = Stream.concat(
            diagram.getTopLevelComponents()
                   .stream()
                   .map(this::renderComponent),
            diagram.getRelationships()
                   .stream()
                   .map(this::renderRelationship)
        )
                                             .collect(joining("\n"));

        final SourceStringReader reader = new SourceStringReader(diagramAsString);
        final PipedInputStream result = new PipedInputStream();
        final PipedOutputStream outputStream = new PipedOutputStream(result);
        final String generationDescription = reader.generateImage(outputStream, new FileFormatOption(fileFormat));
        log.info("diagram generated; PlantUML output: {}", generationDescription);
        return result;
    }

    private String renderComponent(final Component<PlantUMLIdentifier> component) {
        return renderComponent(component, 0);
    }

    private String renderComponent(final Component<PlantUMLIdentifier> component, final int nesting) {
        final ComponentColors componentColors = getComponentColors(nesting);
        return "package <color:" + componentColors.getFontColor()
                                                  .toHexStringNoHash() + ">" + component.getName() + "</color>"
            + " as " + component.getId() + ' ' + componentColors.getBackgroundColor()
                                                                .toHexString() + "{\n"
            + component.getNestedComponents()
                       .stream()
                       .map(nestedComponent -> renderComponent(nestedComponent, nesting + 1))
                       .collect(joining("\n"))
            + '}';
    }

    private static ComponentColors getComponentColors(final int nesting) {
        if (nesting > 5) {
            throw new IllegalArgumentException("only 5 levels of nested components are supported");
        }
        final int backgroundBlack = 100 - (nesting * 20);
        final int fontBlack = backgroundBlack - 50 < 0 ? (backgroundBlack + 50) : backgroundBlack - 50;
        return new ComponentColors(
            new Color(0, 0, 0, backgroundBlack),
            new Color(0, 0, 0, fontBlack)
        );
    }

    private String renderRelationship(final Relationship<PlantUMLIdentifier, ClassRelationship> relationship) {
        final int arrowLength = 1 + random.nextInt(3);
        final Color arrowColor = randomReadableColor(random);

        final String arrow;
        if (relationship.getType() == ClassRelationship.COMPOSES) {
            arrow = "-[" + arrowColor + ",bold]" + repeat(arrowLength, '-') + "|>";
        } else if (relationship.getType() == ClassRelationship.USES) {
            arrow = ".[" + arrowColor.toHexString() + "]" + repeat(arrowLength, '.') + ">";
        } else {
            throw new IllegalArgumentException("unsupported relationship type: " + relationship.getType());
        }

        return relationship.getSource()
                           .getId()
                           .toString()
            + arrow
            + relationship.getDestination()
                          .getId()
                          .toString();
    }

    @Getter(PRIVATE)
    @RequiredArgsConstructor(access = PRIVATE)
    private static final class ComponentColors {

        private final Color backgroundColor;
        private final Color fontColor;

    }

}
