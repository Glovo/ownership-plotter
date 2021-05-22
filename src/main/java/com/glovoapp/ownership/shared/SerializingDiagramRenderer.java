package com.glovoapp.ownership.shared;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.glovoapp.diagrams.Diagram;
import com.glovoapp.diagrams.DiagramRenderer;
import com.glovoapp.diagrams.Identifier;
import com.glovoapp.ownership.plotting.extensions.plantuml.PlantUMLIdentifier;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Simple renderer that outputs the diagram data as serialized String.
 */
@RequiredArgsConstructor
public final class SerializingDiagramRenderer<Id extends Identifier<Id>, RelationshipType>
    implements DiagramRenderer<Id, RelationshipType> {

    private final Function<Diagram<Id, RelationshipType>, String> serializer;

    public static <Id extends Identifier<Id>, RelationshipType> SerializingDiagramRenderer<Id, RelationshipType> ofJackson() {
        final ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(UUIDIdentifier.class, new StdSerializer<UUIDIdentifier>(UUIDIdentifier.class) {
            @Override
            public final void serialize(final UUIDIdentifier value,
                                        final JsonGenerator jsonGenerator,
                                        final SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(value.toString());
            }
        });
        module.addSerializer(PlantUMLIdentifier.class, new StdSerializer<PlantUMLIdentifier>(PlantUMLIdentifier.class) {
            @Override
            public final void serialize(final PlantUMLIdentifier value,
                                        final JsonGenerator jsonGenerator,
                                        final SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(value.toString());

            }
        });
        objectMapper.registerModule(module);

        return new SerializingDiagramRenderer<>(diagram -> {
            try {
                return objectMapper.writeValueAsString(diagram);
            } catch (final JsonProcessingException exception) {
                throw new IllegalArgumentException("failed to serialize with Jackson", exception);
            }
        });
    }

    @Override
    @SneakyThrows
    public final InputStream renderDiagram(final Diagram<Id, RelationshipType> diagram) {
        return new ByteArrayInputStream(
            serializer.apply(diagram)
                      .getBytes()
        );
    }

}
