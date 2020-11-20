package com.glovoapp.ownership.classpath;

import java.util.Set;

public interface ClasspathLoader {

    /**
     * @param packagePrefix only classes in packages whose name starts with this prefix will be loaded
     * @return a collection of all classes within given package
     */
    Set<Class<?>> loadAllClasses(final String packagePrefix);

}
