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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;
import java.util.stream.IntStream;
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

    private static final int MAXIMUM_SUPPORTED_NESTING = 8;

    private final Random random = new Random();
    private final FileFormat fileFormat;

    @Override
    @SneakyThrows
    public final InputStream renderDiagram(final Diagram<PlantUMLIdentifier, ClassRelationship> diagram) {
        final String diagramAsString = "@startuml\n"
            + "hide stereotype\n"
            + "skinparam package {\n"
            + IntStream.rangeClosed(0, MAXIMUM_SUPPORTED_NESTING)
                       .mapToObj(nesting -> {
                           final ComponentColors componentColors = getComponentColors(nesting);
                           return "FontColor<<n" + nesting + ">> " + componentColors.getFontColor()
                                                                                    .toHexString() + '\n'
                               + "BackgroundColor<<n" + nesting + ">> " + componentColors.getBackgroundColor()
                                                                                         .toHexString() + '\n'
                               + "BorderColor<<n" + nesting + ">> " + componentColors.getBorderColor()
                                                                                     .toHexString() + '\n';
                       })
                       .collect(joining("\n"))
            + "\n}\n"
            + "skinparam folder {\n"
            + "shadowing false\n"
            + IntStream.rangeClosed(0, MAXIMUM_SUPPORTED_NESTING)
                       .mapToObj(nesting -> {
                           final ComponentColors componentColors = getComponentColors(nesting);
                           return "FontColor<<n" + nesting + ">> " + componentColors.getBackgroundColor()
                                                                                    .toHexString() + '\n'
                               + "BackgroundColor<<n" + nesting + ">> " + componentColors.getBackgroundColor()
                                                                                         .toHexString() + '\n'
                               + "BorderColor<<n" + nesting + ">> " + componentColors.getBackgroundColor()
                                                                                     .toHexString() + '\n';
                       })
                       .collect(joining("\n"))
            + "\n}\n"
            + Stream.concat(
            diagram.getTopLevelComponents()
                   .stream()
                   .map(this::renderComponent),
            diagram.getRelationships()
                   .stream()
                   .map(this::renderRelationship)
        )
                    .collect(joining("\n"))
            + "\n@enduml";

        final SourceStringReader reader = new SourceStringReader(diagramAsString);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final String generationDescription = reader.outputImage(outputStream, new FileFormatOption(fileFormat))
                                                   .getDescription();
        log.info("diagram generated; PlantUML output: {}", generationDescription);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private String renderComponent(final Component<PlantUMLIdentifier> component) {
        return renderComponent(component, 0);
    }

    private String renderComponent(final Component<PlantUMLIdentifier> component, final int nesting) {
        if (nesting > MAXIMUM_SUPPORTED_NESTING) {
            throw new IllegalArgumentException(
                "only " + MAXIMUM_SUPPORTED_NESTING + " levels of nested components are supported"
            );
        }
        if (component.getName()
                     .isEmpty()) {
            log.warn("found empty component with ID {}, ignoring", component.getId());
            return "";
        }
        final String indent = repeat(nesting * 4, ' ');
        final String folderIndent = repeat((nesting + 1) * 4, ' ');
        return indent + "package \"" + component.getName() + "\" <<n" + nesting + ">> as " + component.getId() + " {\n"
            + (
            component.getNestedComponents()
                     .isEmpty()
                ? folderIndent + "folder " + component.getId() + "_empty <<n" + nesting + ">> {\n" + folderIndent
                + "}\n"
                : component.getNestedComponents()
                           .stream()
                           .map(nestedComponent -> renderComponent(nestedComponent, nesting + 1))
                           .collect(joining("\n"))
        )
            + indent + "}\n";
    }

    private static ComponentColors getComponentColors(final int nesting) {
        final int backgroundBlack = 100 - ((nesting + 2) * 10);
        final int fontBlack = backgroundBlack - 50 < 0 ? (backgroundBlack + 50) : backgroundBlack - 50;
        return new ComponentColors(
            new Color(0, 0, 0, backgroundBlack),
            new Color(0, 0, 0, fontBlack),
            new Color(0, 0, 0, fontBlack)
        );
    }

    private String renderRelationship(final Relationship<PlantUMLIdentifier, ClassRelationship> relationship) {
        final int arrowLength = 1 + random.nextInt(3);
        final Color arrowColor = randomReadableColor(random);

        final String arrow;
        if (relationship.getType() == ClassRelationship.COMPOSES) {
            arrow = "-[" + arrowColor.toHexString() + ",bold]" + repeat(arrowLength, '-') + "|>";
        } else if (relationship.getType() == ClassRelationship.USES) {
            arrow = ".[" + arrowColor.toHexString() + "]" + repeat(arrowLength, '.') + ">";
        } else if (relationship.getType() == ClassRelationship.VISUAL) {
            arrow = "=[hidden]=>";
        } else {
            throw new IllegalArgumentException("unsupported relationship type: " + relationship.getType());
        }

        return relationship.getSource()
                           .getId()
                           .toString()
            + ' ' + arrow + ' '
            + relationship.getDestination()
                          .getId()
                          .toString();
    }

    @Getter(PRIVATE)
    @RequiredArgsConstructor(access = PRIVATE)
    private static final class ComponentColors {

        private final Color backgroundColor;
        private final Color fontColor;
        private final Color borderColor;

    }

}
