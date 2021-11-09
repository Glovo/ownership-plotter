package com.glovoapp.ownership.scanning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ParentPackageAnnotationScanner<A extends Annotation> implements AnnotationScanner<A> {

    private final Class<A> annotationClass;

    @Override
    public Optional<A> scan(AnnotatedElement element){

        log.info("executing ParentPackageAnnotationScanner on element {}", element.toString());

        String packageName = PackageScanningUtils.getPackageName(element).orElse("");

        while (packageName.length() > 0) {
            packageName = PackageScanningUtils.getSuperPackageName(packageName);

            Class<?> packageInfo = null;
            try {
                packageInfo = Class.forName(packageName+".package-info", false, ParentPackageAnnotationScanner.class.getClassLoader());
                if (packageInfo.getPackage().isAnnotationPresent(annotationClass)) {
                    return Optional.of(packageInfo.getAnnotation(annotationClass));
                }
            } catch (ClassNotFoundException e) {

                //TODO add log
                packageInfo = null;
            }
        }

        return Optional.empty();
    }
}
