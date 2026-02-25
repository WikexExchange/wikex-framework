package com.wikex.wikex.annotation;

import com.wikex.wikex.constant.AdminModule;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLog {
    String operation();
    AdminModule module();
}

