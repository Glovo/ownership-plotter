/**
 * Almost all {@link java.lang.ClassLoader} implementations are lazy, meaning that unless we ask for a specific class,
 * it will not be loaded. This prevents us from scanning the classpath for classes that we would like to plot on the
 * diagram. This package contains classes that allow for non-lazy loading of classes.
 */
package com.glovoapp.ownership.classpath;