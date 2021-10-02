package com.glovoapp.ownership.example2;


import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
public @interface OwnerWithContextAnnotation {

    String owner() default "nobody";
    String boundedContext() default "none";
}
