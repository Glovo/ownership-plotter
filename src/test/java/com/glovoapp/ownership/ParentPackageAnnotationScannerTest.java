package com.glovoapp.ownership;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.examples.parentpackage.childpackage.NotAnnotatedClass;
import com.glovoapp.ownership.notannotatedpackage.childpackage.ClassInNotAnnotatedPackage;
import lombok.var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParentPackageAnnotationScannerTest {

    @Test
    void getPackageName_givenClass_shouldReturnPackageName() {

        var sut = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("com.glovoapp.ownership.examples.parentpackage.childpackage", sut.getPackageName(NotAnnotatedClass.class).get());
    }


    @Test
    void getSuperPackageName_givenOneLevel_shouldReturnEmpty() {

        var sut = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("", sut.getSuperPackageName("com"));
    }

    @Test
    void getSuperPackageName_givenTwoLevels_shouldReturnFirstLevel() {

        var sut = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("com", sut.getSuperPackageName("com.glovoapp"));
    }

    @Test
    void getSuperPackageName_givenThreeLevels_shouldReturnSecond() {

        var sut = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertEquals("com.glovoapp", sut.getSuperPackageName("com.glovoapp.ownership"));
    }

    @Test
    void scan_givenClass_shouldFindParentPackageAnnotation() {
        var sut = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertNotNull(sut.scan(NotAnnotatedClass.class).get());
    }

    @Test
    void scan_givenNotAnnotatedPackage_shouldReturnNull() {
        var sut = new ParentPackageAnnotationScanner(ExampleOwnershipAnnotation.class);

        assertFalse(sut.scan(ClassInNotAnnotatedPackage.class).isPresent());
    }
}