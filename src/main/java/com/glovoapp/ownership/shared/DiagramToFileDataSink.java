package com.glovoapp.ownership.shared;

import com.glovoapp.diagrams.DiagramDataSink;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@RequiredArgsConstructor
public final class DiagramToFileDataSink implements DiagramDataSink {

    private final File file;

    @Override
    @SneakyThrows
    public final void accept(final InputStream dataStream) {
        final OutputStream outputStream = new FileOutputStream(file);

        final byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = dataStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        dataStream.close();
        outputStream.close();
    }

}
