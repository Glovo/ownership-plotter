package com.glovoapp.ownership.plotting;

import com.glovoapp.ownership.ClassOwnership;
import com.glovoapp.ownership.ClassOwnershipExtractor;
import com.glovoapp.ownership.classpath.ClasspathLoader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
@RequiredArgsConstructor
public final class ClassOwnershipPlotter {

    private final ClasspathLoader classpathLoader;
    private final ClassOwnershipExtractor extractor;
    private final DomainOwnershipFilter filter;
    private final OwnershipDiagramPipeline diagramPipeline;

    /**
     * This method will load all classes in given package for analysis using {@link Reflections#getSubTypesOf(Class)}.
     * We need to do this because class loaders are lazy and wouldn't load classes unless explicitly asked for them. All
     * other classes currently loaded with {@link Thread#getContextClassLoader() context class loader} will be included
     * as well.
     *
     * @param packagePrefix only classes from this package will be loaded, e.g. "com.example"
     */
    public final void createClasspathDiagram(final String packagePrefix) {
        createDiagram(
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
    public final void createDiagram(final Set<Class<?>> domain) {
        final Set<ClassOwnership> filteredDomainOwnership = extractAndFilter(domain);
        diagramPipeline.createDiagram(filteredDomainOwnership);
    }

    private Set<ClassOwnership> extractAndFilter(final Set<Class<?>> domain) {
        final Set<ClassOwnership> fullDomainOwnership = domain.stream()
                                                              .map(extractor::getOwnershipOf)
                                                              .filter(Optional::isPresent)
                                                              .map(Optional::get)
                                                              .collect(Collectors.toSet());

        return filter.apply(fullDomainOwnership);
    }

}
