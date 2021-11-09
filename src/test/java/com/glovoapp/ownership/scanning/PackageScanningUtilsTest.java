package com.glovoapp.ownership.scanning;

import com.glovoapp.ownership.examples.parentpackage.childpackage.NotAnnotatedClass;
import org.junit.jupiter.api.Test;

import static com.glovoapp.ownership.scanning.PackageScanningUtils.getPackageName;
import static com.glovoapp.ownership.scanning.PackageScanningUtils.getSuperPackageName;
import static org.junit.jupiter.api.Assertions.*;

class PackageScanningUtilsTest {

    @Test
    void getPackageName_givenClass_shouldReturnPackageName() {

        assertEquals("com.glovoapp.ownership.examples.parentpackage.childpackage", getPackageName(NotAnnotatedClass.class).get());
    }


    @Test
    void getSuperPackageName_givenOneLevel_shouldReturnEmpty() {

        assertEquals("", getSuperPackageName("com"));
    }

    @Test
    void getSuperPackageName_givenTwoLevels_shouldReturnFirstLevel() {

        assertEquals("com", getSuperPackageName("com.glovoapp"));
    }

    @Test
    void getSuperPackageName_givenThreeLevels_shouldReturnSecond() {

        assertEquals("com.glovoapp", getSuperPackageName("com.glovoapp.ownership"));
    }



}