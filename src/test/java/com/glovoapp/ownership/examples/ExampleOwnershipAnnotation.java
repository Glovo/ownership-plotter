package com.glovoapp.ownership.examples;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ExampleOwnershipAnnotation {

    ExampleOwner owner();

    ExampleFeature[] features() default {};

}
