package com.glovoapp.ownership.plotting.extensions.html;

import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.DiagramRenderer;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.ownership.plotting.ClassRelationship;
import com.glovoapp.ownership.shared.BufferedDiagramDataSink;
import com.glovoapp.ownership.shared.SerializingDiagramRenderer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public final class HTMLTemplateDiagramRenderer<Id extends Identifier<Id>> implements DiagramRenderer<Id, ClassRelationship> {

    private static final String RAW_DIAGRAM_DATA_META_ELEMENT_ID = "rawDiagramData";

    public enum Template {
        EXAMPLE, TREEMAP;

        private String getTemplateResourceName() {
            return "html_diagram_templates/" + name().toLowerCase(Locale.ENGLISH) + ".html";
        }
    }

    private final Document htmlTemplate;
    private final SerializingDiagramRenderer<Id, ClassRelationship> delegate;

    public HTMLTemplateDiagramRenderer(final Template template) {
        this(template, SerializingDiagramRenderer.ofJackson());
    }

    public HTMLTemplateDiagramRenderer(final Template template,
                                       final SerializingDiagramRenderer<Id, ClassRelationship> delegate) {
        this(
                HTMLTemplateDiagramRenderer.class.getClassLoader()
                        .getResourceAsStream(template.getTemplateResourceName()),
                delegate
        );
    }

    public HTMLTemplateDiagramRenderer(final InputStream htmlTemplate) {
        this(htmlTemplate, SerializingDiagramRenderer.ofJackson());
    }

    @SneakyThrows
    public HTMLTemplateDiagramRenderer(final InputStream htmlTemplate,
                                       final SerializingDiagramRenderer<Id, ClassRelationship> delegate) {
        this.delegate = delegate;
        this.htmlTemplate = Jsoup.parse(htmlTemplate, UTF_8.name(), "");
    }

    @Override
    @SneakyThrows
    public InputStream renderDiagram(final Diagram<Id, ClassRelationship> diagram) {
        final Document htmlDiagram = htmlTemplate.clone();

        final Element rawDiagramDataElement = Optional
                .ofNullable(
                        htmlDiagram.getElementsByAttributeValue("name", RAW_DIAGRAM_DATA_META_ELEMENT_ID)
                                .addClass("meta")
                                .first()
                )
                .orElseGet(() -> {
                    final Element newElement = htmlDiagram.createElement("meta");
                    newElement.attr("name", RAW_DIAGRAM_DATA_META_ELEMENT_ID);
                    htmlDiagram.head()
                            .appendChild(newElement);
                    return newElement;
                });

        final InputStream serializedDiagram = delegate.renderDiagram(diagram);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BufferedDiagramDataSink delegate = new BufferedDiagramDataSink(outputStream);
        delegate.accept(serializedDiagram);
        rawDiagramDataElement.attr("content", outputStream.toString());

        return new ByteArrayInputStream(htmlDiagram.toString()
                .getBytes(UTF_8));
    }


}
