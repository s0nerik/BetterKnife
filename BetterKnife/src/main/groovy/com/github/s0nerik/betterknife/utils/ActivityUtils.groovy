package com.github.s0nerik.betterknife.utils
import android.os.Bundle
import com.github.s0nerik.betterknife.annotations.InjectLayout
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
final class ActivityUtils {

    /**
     *
     * @return onCreate method with super call as a first line.
     */
    static MethodNode createOnCreateMethod() {
        /*
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState)
        }
        */
        def node = AstUtils.createMethod "onCreate", [
                modifiers: Opcodes.ACC_PROTECTED,
                params   : params(param(ClassHelper.make(Bundle), "savedInstanceState")),
                code     : block(createSuperOnCreateCall("savedInstanceState"))
        ]

        node.addAnnotation(new AnnotationNode(ClassHelper.make(Override)))

        return node
    }

    static MethodNode findOnCreateMethod(ClassNode activityClass) {
        AstUtils.findDeclaredMethod(activityClass, "onCreate", params(param(ClassHelper.make(Bundle), "savedInstanceState")))
    }

    /**
     *
     * @param bundleName
     * @return super.onCreate(bundleName) statement
     */
    static Statement createSuperOnCreateCall(String bundleName) {
        // super.onCreate(bundleName)
        return AstUtils.createSuperCallStatement("onCreate", varX(bundleName, ClassHelper.make(Bundle)))
    }

    /**
     *
     * @param id Layout id
     * @return setContentView(id) statement
     */
    static Statement createSetContentView(Expression id) {
        // setContentView(id)
        return stmt(callThisX("setContentView", id))
    }

    static void injectViews(ClassNode activityClass) throws Exception {
        if(isActivityHasInjectedParent(activityClass)) {
            println "${activityClass} has injected parent"
            return
        }
        println "$activityClass has no injected parent"

        def onCreateMethod = findOnCreateMethod(activityClass)
        if (!onCreateMethod) {
            onCreateMethod = createOnCreateMethod()
            activityClass.addMethod(onCreateMethod)
        }

        if (onCreateMethod.code instanceof BlockStatement) {
            def onCreateMethodStatements = AstUtils.getMethodStatements(onCreateMethod)

//            def injectCall = InjectionUtils.createInjectViewsCall(callThisX("findViewById", varX("android.R.id.content")))
            def injectCall = InjectionUtils.createInjectViewsCall(callThisX("findViewById", fieldX(ClassHelper.make(android.R.id), "content")))
//            def injectCall = InjectionUtils.createInjectViewsCall(callThisX("findViewById", varX("this")))

            if (!AstUtils.findStatementByLabel(onCreateMethodStatements, injectCall.statementLabel)) {
                int contentViewIndex = AstUtils.findSetterCallPosition(onCreateMethod.code as BlockStatement, "contentView")
                int injectionIndex
                if (contentViewIndex < 0) {
                    if (activityClass.getAnnotations(ClassHelper.make(InjectLayout))) {
                        injectionIndex = 2
                    } else {
                        throw new Exception("Can't find set${"contentView".capitalize()}() method call or contentView property setted.")
                    }
                } else {
                    injectionIndex = contentViewIndex + 1
                }
                if (injectionIndex < onCreateMethodStatements.size()) {
                    onCreateMethodStatements.add(injectionIndex, injectCall)
                } else {
                    onCreateMethodStatements << injectCall
                }
            }
        }

    }

    static boolean isActivityHasInjectedParent(ClassNode activityClass) {
        def onCreateMethods = activityClass.getMethods("onCreate").findAll {it.parameters.size() == 1}

        return  AstUtils.superClassHasAnnotation(activityClass, InjectLayout) ||
                AstUtils.superClassHasFieldWithAnnotation(activityClass, InjectView) ||
                onCreateMethods.find {
                    AstUtils.findStatementByLabel(AstUtils.getMethodStatements(it), "_injectViews")
                }
    }

}