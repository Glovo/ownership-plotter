package com.glovoapp.ownership.classpath;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toSet;

/**
 * Uses all currently loaded classes as the classpath definition. This version of {@link ClasspathLoader} is much more
 * efficient than {@link ReflectionsClasspathLoader} but it will only consider classes that have been loaded before the
 * {@link #loadAllClasses(String)} method was called.
 */
@Slf4j
@RequiredArgsConstructor
public final class ClassLoaderClasspathLoader implements ClasspathLoader {

    private final ClassLoader classLoader;

    /**
     * Uses context class loader from {@link Thread}.{@link Thread#currentThread() currentThread()}.{@link
     * Thread#getContextClassLoader() getContextClassLoader()}.
     */
    public ClassLoaderClasspathLoader() {
        this(currentThread().getContextClassLoader());
    }

    @Override
    @SneakyThrows
    public final Set<Class<?>> loadAllClasses(final String packagePrefix) {
        log.info("fetching classes from {}", classLoader);
        final Field classesField = ClassLoader.class.getDeclaredField("classes");
        classesField.setAccessible(true);
        @SuppressWarnings("unchecked") final Vector<Class<?>> classesVector
            = (Vector<Class<?>>) classesField.get(classLoader);
        final Set<Class<?>> classesSet = new HashSet<>(classesVector);
        final Set<Class<?>> parentClassesSet = Optional.of(classLoader)
                                                       .map(ClassLoader::getParent)
                                                       .map(ClassLoaderClasspathLoader::new)
                                                       .map(parentLoader -> parentLoader.loadAllClasses(packagePrefix))
                                                       .orElseGet(Collections::emptySet);
        return Stream.concat(classesSet.stream(), parentClassesSet.stream())
                     .filter(ClassLoaderClasspathLoader::isProperlyLoaded)
                     .filter(theClass -> classIsInPackageWithPrefix(theClass, packagePrefix))
                     .collect(toSet());
    }

    private static boolean isProperlyLoaded(final Class<?> theClass) {
        try {
            return theClass.getCanonicalName() != null;
        } catch (final NoClassDefFoundError error) {
            log.info("definition of class {} has not been found", theClass);
            return false;
        }
    }

    private static boolean classIsInPackageWithPrefix(final Class<?> theClass, final String packagePrefix) {
        return Optional.of(theClass)
                       .map(Class::getPackage)
                       .map(Package::getName)
                       .map(packageName -> packageName.startsWith(packagePrefix))
                       .orElse(false);
    }

}
