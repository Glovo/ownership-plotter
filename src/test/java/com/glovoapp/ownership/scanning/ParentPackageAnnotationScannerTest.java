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
    void getPackageName_givenClass_shouldReturnPackageName() {

        var scanner = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("com.glovoapp.ownership.examples.parentpackage.childpackage", scanner.getPackageName(NotAnnotatedClass.class).get());
    }


    @Test
    void getSuperPackageName_givenOneLevel_shouldReturnEmpty() {

        var scanner = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("", scanner.getSuperPackageName("com"));
    }

    @Test
    void getSuperPackageName_givenTwoLevels_shouldReturnFirstLevel() {

        var scanner = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("com", scanner.getSuperPackageName("com.glovoapp"));
    }

    @Test
    void getSuperPackageName_givenThreeLevels_shouldReturnSecond() {

        var scanner = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("com.glovoapp", scanner.getSuperPackageName("com.glovoapp.ownership"));
    }

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