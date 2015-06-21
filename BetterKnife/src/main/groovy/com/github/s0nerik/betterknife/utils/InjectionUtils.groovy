package com.github.s0nerik.betterknife.utils

import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.Statement

@CompileStatic
final class InjectionUtils {
    static Statement createInjectViewsCall() {
        return AstUtils.createMethodCallStatementWithLabel("_injectViews")
    }

    static MethodNode getInjectViewsMethod(ClassNode classNode, ClassNode annotationClass = ClassHelper.make(InjectView)) {
        MethodNode injectMethod = AstUtils.findMethod(classNode, "_injectViews", 0)

        if (!injectMethod) {
            injectMethod = AstUtils.createMethod "_injectViews"
            if (AstUtils.superClassHasFieldWithAnnotation(classNode, annotationClass)) {
                // super._injectViews()
                AstUtils.getMethodStatements(injectMethod) << AstUtils.createSuperCallStatement("_injectViews")
            }
            classNode.addMethod injectMethod
        }

        return injectMethod
    }

}