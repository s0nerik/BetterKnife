package com.github.s0nerik.betterknife.utils
import android.app.Activity
import android.support.v4.app.Fragment
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.Statement

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

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

    /**
     *
     * @param id
     * @return resources.getIdentifier(id, "id", activity.getPackageName())
     */
    static Expression createGetIdentifier(ClassNode declaringClass, String id) {
        Expression expression
        if (AstUtils.isSubclass(declaringClass, Activity)) {
            // resources.getIdentifier("viewName", "id", getPackageName())
            expression = callX(
                    varX("resources"),
                    "getIdentifier",
                    args(
                            constX(id),
                            constX("id"),
                            callThisX("getPackageName")
                    )
            )
        } else if (AstUtils.isSubclass(declaringClass, Fragment)) {
            // resources.getIdentifier("viewName", "id", activity.getPackageName())
            expression = callX(
                    varX("resources"),
                    "getIdentifier",
                    args(
                            constX(id),
                            constX("id"),
                            callX(varX("activity"), "getPackageName")
                    )
            )
        }
        return expression
    }

    /**
     * Creates a single view injection line that should be added to the _injectViews() method.
     * @param fieldNode View to inject into
     * @param id Resource id
     * @return Single view injection line
     */
    private static Statement createFindViewByIdStatement(FieldNode fieldNode, Expression id) {
        Statement statement
        if (AstUtils.isSubclass(fieldNode.declaringClass, Activity)) {
            // field = (field.type) findViewById(id)
            statement = assignS(varX(fieldNode), castX(fieldNode.type, callThisX('findViewById', id)))
        } else if (AstUtils.isSubclass(fieldNode.declaringClass, Fragment)) {
            // field = (field.type) view.findViewById(id)
            statement = assignS(varX(fieldNode), castX(fieldNode.type, callX(varX("view"), 'findViewById', id)))
        }
        return statement
    }

    /**
     * Adds a single view injection line into _injectViews() method.
     * @param methodNode Method where the statement should be appended
     * @param fieldNode View to inject into
     * @param id Resource id
     */
    static void appendFindViewByIdStatement(MethodNode methodNode, FieldNode fieldNode, Expression id) {
        AstUtils.appendStatement(methodNode, createFindViewByIdStatement(fieldNode, id))
    }

    /**
     * Adds a single view injection line into _injectViews() method.
     * @param methodNode Method where the statement should be appended
     * @param fieldNode View to inject into
     * @param id Resource id
     */
    static void appendFindViewByIdStatement(MethodNode methodNode, FieldNode fieldNode, String idString) {
        def findLowerCased = createFindViewByIdStatement(fieldNode, createGetIdentifier(methodNode.declaringClass, AnnotationUtils.camelCaseToLowerCase(idString)))
        def findOriginal = createFindViewByIdStatement(fieldNode, createGetIdentifier(methodNode.declaringClass, idString))

        AstUtils.appendStatements(methodNode, [findLowerCased, ifS(equalsNullX(args(fieldNode.name)), findOriginal)])
    }

}