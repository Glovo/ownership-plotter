package com.glovoapp.ownership.scanning;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.examples.parentpackage.childpackage.NotAnnotatedClass;
import com.glovoapp.ownership.notannotatedpackage.childpackage.ClassInNotAnnotatedPackage;
import com.glovoapp.ownership.scanning.ParentPackageAnnotationScanner;
import lombok.var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParentPackageAnnotationScannerTest {

    @Test
    void scan_givenClass_shouldFindParentPackageAnnotation() {
        var scanner = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertNotNull(scanner.scan(NotAnnotatedClass.class).get());
    }

    @Test
    void scan_givenNotAnnotatedPackage_shouldReturnNull() {
        var scanner = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertFalse(scanner.scan(ClassInNotAnnotatedPackage.class).isPresent());
    }
}