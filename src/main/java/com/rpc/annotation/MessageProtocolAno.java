package com.rpc.annotation;

import java.lang.annotation.*;

/**
 * 序列化注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageProtocolAno {

    String value() default "";
}
