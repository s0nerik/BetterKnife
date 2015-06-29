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
public @interface OnItemSelected {
    int[] value();

    @CompileStatic
    public static final enum Method {
        ITEM_SELECTED,
        NOTHING_SELECTED
    }
}
