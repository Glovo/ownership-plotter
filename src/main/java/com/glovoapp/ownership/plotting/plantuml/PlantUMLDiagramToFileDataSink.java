package com.glovoapp.ownership.plotting.plantuml;

import com.glovoapp.diagrams.DiagramDataSink;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.SneakyThrows;
import net.sourceforge.plantuml.FileFormat;


public final class PlantUMLDiagramToFileDataSink implements DiagramDataSink {

    private final File file;

    public PlantUMLDiagramToFileDataSink(final FileFormat fileFormat, final File file) {
        final String absolutePath = file.getAbsolutePath();
        final String expectedExtension = fileFormat.getFileSuffix();
        if (!absolutePath.endsWith(expectedExtension)) {
            throw new IllegalArgumentException(
                "file " + absolutePath + " has invalid extension, expected " + expectedExtension
            );
        }
        this.file = file;
    }

    @Override
    @SneakyThrows
    public final void accept(final InputStream dataStream) {
        final OutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = dataStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        dataStream.close();
        outputStream.close();
    }

}
