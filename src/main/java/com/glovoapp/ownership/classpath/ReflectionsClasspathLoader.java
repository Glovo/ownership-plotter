package com.glovoapp.ownership.classpath;

import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

/**
 * {@link ClasspathLoader} that uses {@link Reflections} for loading classes.
 */
public final class ReflectionsClasspathLoader implements ClasspathLoader {

    @Override
    public final Set<Class<?>> loadAllClasses(final String packagePrefix) {
        Reflections reflections = new Reflections(packagePrefix, new SubTypesScanner(false));
        return reflections.getSubTypesOf(Object.class);
    }

}
