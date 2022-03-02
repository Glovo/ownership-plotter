package com.glovoapp.ownership.shared;

import com.glovoapp.diagrams.DiagramDataSink;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Sink that prints the diagram data to {@link System#out}.
 */
public final class DiagramToConsoleDataSink implements DiagramDataSink {

    @Override
    public final void accept(final InputStream diagramDataStream) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BufferedDiagramDataSink delegate = new BufferedDiagramDataSink(outputStream);

        delegate.accept(diagramDataStream);

        System.out.println(outputStream);
    }

}
