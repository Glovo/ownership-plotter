package com.glovoapp.ownership.plotting.plantuml;

import com.glovoapp.ownership.plotting.DiagramFactory;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

@Slf4j
@RequiredArgsConstructor
public final class PlantUMLDiagramDataFactory implements DiagramFactory<SourceStringReader> {

    private final FileFormat fileFormat;

    @Override
    @SneakyThrows
    public final void generateDiagram(final SourceStringReader diagramData, final OutputStream outputStream) {
        String generationDescription = diagramData.generateImage(outputStream, new FileFormatOption(fileFormat));
        log.info("diagram generated; PlantUML output: {}", generationDescription);
    }

}
