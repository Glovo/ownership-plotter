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
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

@Slf4j
@RequiredArgsConstructor
public final class ClassOwnershipPlotter {

    private final ClassOwnershipExtractor extractor;
    private final DiagramDataPipeline diagramDataPipeline;

    /**
     * This method will load all classes in given package for analysis using {@link Reflections#getSubTypesOf(Class)}.
     * We need to do this because class loaders are lazy and wouldn't load classes unless explicitly asked for them. All
     * other classes currently loaded with {@link Thread#getContextClassLoader() context class loader} will be included
     * as well.
     *
     * @param packagePrefix only classes from this package will be loaded, e.g. "com.example"
     * @param fileName      name of the file to save the result to
     */
    public final void writeDiagramOfClasspathToFile(final String packagePrefix, final String fileName) {
        loadAllClassesWithPrefix(packagePrefix);
        writeDiagramToFile(
            fileName,
            getLoadedClassesFrom(currentThread().getContextClassLoader())
                .stream()
                .filter(aClass -> Optional.of(aClass)
                                          .map(Class::getPackage)
                                          .map(Package::getName)
                                          .map(packageName -> packageName.startsWith(packagePrefix))
                                          .orElse(false))
                .collect(toSet())
        );
    }

    @SneakyThrows
    public final void writeDiagramToFile(final String fileName,
                                         final Collection<Class<?>> domain) {
        final Set<ClassOwnership> domainOwnership = domain.stream()
                                                          .map(extractor::getOwnershipOf)
                                                          .filter(Optional::isPresent)
                                                          .map(Optional::get)
                                                          .collect(Collectors.toSet());

        final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        diagramDataPipeline.generateDiagram(domainOwnership, fileOutputStream);
        fileOutputStream.close();
    }

    private static void loadAllClassesWithPrefix(final String packagePrefix) {
        Reflections reflections = new Reflections(packagePrefix, new SubTypesScanner(false));
        reflections.getSubTypesOf(Object.class);
    }

    @SneakyThrows
    private static Set<Class<?>> getLoadedClassesFrom(final ClassLoader classLoader) {
        log.info("fetching classes from {}", classLoader);
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
                             log.info("definition of class {} has not been found", it);
                             return false;
                         }
                     })
                     .collect(toSet());
    }

}
