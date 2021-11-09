package com.glovoapp.ownership.scanning;

import com.glovoapp.ownership.examples.ExampleOwnershipAnnotation;
import com.glovoapp.ownership.examples.parentpackage.childpackage.AnotherNotAnnotatedClass;
import com.glovoapp.ownership.examples.parentpackage.childpackage.NotAnnotatedClass;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CachedParentPackageAnnotationScannerTest {

    ParentPackageAnnotationScanner<ExampleOwnershipAnnotation> delegate = mock(ParentPackageAnnotationScanner.class);

    @Test
    void givenClassesFromSamePackage_whenScan_shouldInvokeDelegateOnce() {

        when(delegate.scan(any())).thenReturn(Optional.empty());
        var scanner = new CachedParentPackageAnnotationScanner(delegate);

        scanner.scan(NotAnnotatedClass.class);
        scanner.scan(AnotherNotAnnotatedClass.class);

        verify(delegate, times(1)).scan((any()));

    }
}