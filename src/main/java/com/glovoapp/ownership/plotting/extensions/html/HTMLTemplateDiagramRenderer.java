package com.glovoapp.ownership.plotting.extensions.html;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.DiagramRenderer;
import com.glovoapp.ownership.plotting.ClassRelationship;
import com.glovoapp.ownership.shared.BufferedDiagramDataSink;
import com.glovoapp.ownership.shared.SerializingDiagramRenderer;
import com.glovoapp.ownership.shared.UUIDIdentifier;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Slf4j
public final class HTMLTemplateDiagramRenderer implements DiagramRenderer<UUIDIdentifier, ClassRelationship> {

    private static final String RAW_DIAGRAM_DATA_META_ELEMENT_ID = "rawDiagramData";

    public enum Template {
        EXAMPLE, TREEMAP;

        private String getTemplateResourceName() {
            return "html_diagram_templates/" + name().toLowerCase(Locale.ENGLISH) + ".html";
        }
    }

    private final Document htmlTemplate;
    private final SerializingDiagramRenderer<UUIDIdentifier, ClassRelationship> delegate;

    public HTMLTemplateDiagramRenderer(final Template template) {
        this(template, SerializingDiagramRenderer.ofJackson());
    }

    public HTMLTemplateDiagramRenderer(final Template template,
                                       final SerializingDiagramRenderer<UUIDIdentifier, ClassRelationship> delegate) {
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
                                       final SerializingDiagramRenderer<UUIDIdentifier, ClassRelationship> delegate) {
        this.delegate = delegate;
        this.htmlTemplate = Jsoup.parse(htmlTemplate, UTF_8.name(), "");
    }

    @Override
    @SneakyThrows
    public final InputStream renderDiagram(final Diagram<UUIDIdentifier, ClassRelationship> diagram) {
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
