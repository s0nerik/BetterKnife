package com.github.s0nerik.betterknife.annotations

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(classes = [InjectLayoutTransformation, InjectUITransformation])
@interface InjectLayout {
    int value() default -1;
    boolean injectAllViews() default false;
}
