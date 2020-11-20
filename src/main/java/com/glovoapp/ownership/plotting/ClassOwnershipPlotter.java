package com.glovoapp.ownership.plotting;

import static java.util.stream.Collectors.toSet;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.ClassOwnershipExtractor;
import com.glovoapp.ownership.classpath.ClasspathLoader;
import com.glovoapp.ownership.plotting.OwnershipFilter.OwnershipContext;
import java.io.FileOutputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

@Slf4j
@RequiredArgsConstructor
public final class ClassOwnershipPlotter {

    private final ClasspathLoader classpathLoader;
    private final ClassOwnershipExtractor extractor;
    private final OwnershipFilter filter;
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
        writeDiagramToFile(
            fileName,
            classpathLoader.loadAllClasses(packagePrefix)
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
                                         final Set<Class<?>> domain) {
        final Set<ClassOwnership> filteredDomainOwnership = extractAndFilter(domain);

        final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        diagramDataPipeline.generateDiagram(filteredDomainOwnership, fileOutputStream);
        fileOutputStream.close();
    }

    private Set<ClassOwnership> extractAndFilter(final Set<Class<?>> domain) {
        final Set<ClassOwnership> fullDomainOwnership = domain.stream()
                                                              .map(extractor::getOwnershipOf)
                                                              .filter(Optional::isPresent)
                                                              .map(Optional::get)
                                                              .collect(Collectors.toSet());

        return fullDomainOwnership.stream()
                                  .filter(
                                      ownership -> filter.test(new OwnershipContext(ownership, fullDomainOwnership))
                                  )
                                  .collect(toSet());
    }

}
