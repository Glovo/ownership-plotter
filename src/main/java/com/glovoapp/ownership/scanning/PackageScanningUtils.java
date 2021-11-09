package com.glovoapp.ownership.scanning;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

public class PackageScanningUtils {

    static Optional<String> getPackageName(AnnotatedElement element) {

        if (element instanceof Class) {
            return Optional.of(((Class<?>) element).getPackage().getName());
        }
        else if (element instanceof Method) {
            return Optional.of(((Method) element).getDeclaringClass().getPackage().getName());
        }

        return Optional.empty();
    }

    static String getSuperPackageName(String packageName) {
        return packageName.substring(0, Math.max(packageName.lastIndexOf('.'), 0));
    }
}
