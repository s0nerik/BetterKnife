package com.github.s0nerik.betterknife.annotations

import groovy.transform.CompileStatic
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by Arasthel on 16/08/14.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@GroovyASTTransformationClass(classes = [InjectViewTransformation])
public @interface OnTextChanged {
    int[] value();

    OnTextChanged.Method method();

    @CompileStatic
    public static final enum Method {
        BEFORE_TEXT_CHANGED,
        ON_TEXT_CHANGED,
        AFTER_TEXT_CHANGED
    }
}