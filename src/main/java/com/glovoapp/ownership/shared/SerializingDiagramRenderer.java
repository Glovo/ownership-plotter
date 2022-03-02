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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * Simple renderer that outputs the diagram data as serialized String.
 */
@RequiredArgsConstructor
public final class SerializingDiagramRenderer<Id extends Identifier<Id>, RelationshipType>
        implements DiagramRenderer<Id, RelationshipType> {

    private final Function<Diagram<Id, RelationshipType>, String> serializer;

    @SuppressWarnings("rawtypes")
    public static <Id extends Identifier<Id>, RelationshipType> SerializingDiagramRenderer<Id, RelationshipType> ofJackson() {
        final ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Identifier.class, new StdSerializer<Identifier>(Identifier.class) {
            @Override
            public void serialize(final Identifier value,
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
    public InputStream renderDiagram(final Diagram<Id, RelationshipType> diagram) {
        return new ByteArrayInputStream(
                serializer.apply(diagram)
                        .getBytes()
        );
    }

}
