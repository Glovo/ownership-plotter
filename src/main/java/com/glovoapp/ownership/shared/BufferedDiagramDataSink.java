package com.glovoapp.ownership.shared;

import com.glovoapp.diagrams.DiagramDataSink;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public final class BufferedDiagramDataSink implements DiagramDataSink {

    private final int bufferSize;
    private final OutputStream outputStream;

    public BufferedDiagramDataSink(final OutputStream outputStream) {
        this(8 * 1024, outputStream);
    }

    @Override
    @SneakyThrows
    public final void accept(final InputStream diagramDataStream) {
        final byte[] buffer = new byte[bufferSize];

        int bytesRead;
        while ((bytesRead = diagramDataStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        diagramDataStream.close();
        outputStream.close();
    }

}
