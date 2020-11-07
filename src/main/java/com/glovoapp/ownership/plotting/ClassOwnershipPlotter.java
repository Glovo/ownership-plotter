package com.glovoapp.ownership.plotting;

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toSet;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.ClassOwnershipExtractor;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public final class ClassOwnershipPlotter {

    private final ClassOwnershipExtractor extractor;
    private final DiagramDataPipeline diagramDataPipeline;

    public final void writeDiagramOfClassesLoadedInContextToFile(final String fileName) {
        writeDiagramToFile(fileName, getLoadedClassesFrom(currentThread().getContextClassLoader()));
    }

    @SneakyThrows
    public final void writeDiagramToFile(final String fileName, final Collection<Class<?>> domain) {
        final Set<ClassOwnership> domainOwnership = domain.stream()
                                                          .map(extractor::getOwnershipOf)
                                                          .filter(Optional::isPresent)
                                                          .map(Optional::get)
                                                          .collect(Collectors.toSet());

        final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        diagramDataPipeline.generateDiagram(domainOwnership, fileOutputStream);
        fileOutputStream.close();
    }

    @SneakyThrows
    private static Set<Class<?>> getLoadedClassesFrom(final ClassLoader classLoader) {
        log.info("fetching classes from " + classLoader);
        final Field classesField = ClassLoader.class.getDeclaredField("classes");
        classesField.setAccessible(true);
        @SuppressWarnings("unchecked") final Vector<Class<?>> classesVector
            = (Vector<Class<?>>) classesField.get(classLoader);
        final Set<Class<?>> classesSet = new HashSet<>(classesVector);
        final Set<Class<?>> parentClassesSet = Optional.of(classLoader)
                                                       .map(ClassLoader::getParent)
                                                       .map(ClassOwnershipPlotter::getLoadedClassesFrom)
                                                       .orElseGet(Collections::emptySet);
        return Stream.concat(classesSet.stream(), parentClassesSet.stream())
                     .filter(it -> {
                         try {
                             return it.getCanonicalName() != null;
                         } catch (final NoClassDefFoundError error) {
                             log.info("definition of class " + it + " has not been found");
                             return false;
                         }
                     })
                     .collect(toSet());
    }

}
