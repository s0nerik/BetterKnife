package com.github.s0nerik.betterknife.utils

import android.app.Activity
import android.support.v4.app.Fragment
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import com.github.s0nerik.betterknife.annotations.InjectView
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.Statement

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
final class InjectionUtils {
    static Statement createInjectViewsCall() {
        return AstUtils.createMethodCallStatementWithLabel("_injectViews")
    }

    static boolean hasInjectViewsMethod(ClassNode classNode) {
//        if (AstUtils.findMethod(classNode, "_injectViews", 0)) {
//            return true
//        }
//        return false
        return AstUtils.findMethod(classNode, "_injectViews", 0) as boolean
    }

    static MethodNode getOrCreateInjectViewsMethod(ClassNode classNode, ClassNode annotationClass = ClassHelper.make(InjectView)) {
        MethodNode injectMethod = AstUtils.findMethod(classNode, "_injectViews", 0)

        if (!injectMethod) {
            injectMethod = AstUtils.createMethod "_injectViews"
            if (AstUtils.superClassHasFieldWithAnnotation(classNode, annotationClass)) {
                // super._injectViews()
                AstUtils.appendStatement(injectMethod, AstUtils.createSuperCallStatement("_injectViews"))
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
        Expression getPackageNameX

        if (AstUtils.isSubclass(declaringClass, Activity)) {
            // resources.getIdentifier("viewName", "id", getPackageName())
            getPackageNameX = callThisX("getPackageName")
        } else if (AstUtils.isSubclass(declaringClass, Fragment)) {
            // resources.getIdentifier("viewName", "id", activity.getPackageName())
            getPackageNameX = callX(varX("activity"), "getPackageName")
        } else {
            throw new Exception("getIdentifier can only be used inside Activity or Fragment")
        }

        return callX(varX("resources"), "getIdentifier", args( constX(id), constX("id"), getPackageNameX))
    }

    /**
     * Creates a single view injection line that should be added to the _injectViews() method.
     * @param fieldNode View to inject into
     * @param id Resource id
     * @return Single view injection line
     */
    private static Statement createFindViewByIdStatement(ClassNode declaringClass, VariableExpression fieldNode, Expression id) {
        return assignS(fieldNode, createFindViewByIdX(declaringClass, fieldNode.type, id))
    }

    private static Expression createFindViewByIdX(ClassNode declaringClass, ClassNode viewClass, Expression id) {
        Expression expression
        if (AstUtils.isSubclass(declaringClass, Activity)) {
            // (viewClass) findViewById(id)
            expression = castX(viewClass, callThisX('findViewById', id))
        } else if (AstUtils.isSubclass(declaringClass, Fragment)) {
            // (viewClass) view.findViewById(id)
            expression = castX(viewClass, callX(varX("view"), 'findViewById', id))
        } else {
            throw new Exception("findViewById can only be used inside Activity or Fragment")
        }
        return expression
    }

    /**
     * Adds a single view injection line into _injectViews() method.
     * @param methodNode Method where the statement should be appended
     * @param fieldNode View to inject into
     * @param id Resource id
     */
    static void appendFindViewByIdStatement(MethodNode methodNode, FieldNode fieldNode, Expression id) {
        AstUtils.appendStatement(methodNode, createFindViewByIdStatement(methodNode.declaringClass, varX(fieldNode), id))
    }

    /**
     * Adds a single view injection line into _injectViews() method.
     * @param methodNode Method where the statement should be appended
     * @param fieldNode View to inject into
     * @param id Resource id as a String
     */
    static void appendFindViewByIdStatement(MethodNode methodNode, FieldNode fieldNode, String idString) {
        def findLowerCased = createFindViewByIdStatement(methodNode.declaringClass, varX(fieldNode), createGetIdentifier(methodNode.declaringClass, AnnotationUtils.camelCaseToLowerCase(idString)))
        def findOriginal = createFindViewByIdStatement(methodNode.declaringClass, varX(fieldNode), createGetIdentifier(methodNode.declaringClass, idString))

        AstUtils.appendStatements(methodNode, [findLowerCased, ifS(equalsNullX(varX(fieldNode)), findOriginal)])
    }

//    static void appendOnClickListenerStatement(MethodNode injectionMethod, MethodNode listener, Expression id) {
////        def shareVariables = { VariableScope variableScope ->
////            def scope = variableScope.copy()
////            for (Iterator<Variable> vars = scope.referencedLocalVariablesIterator; vars.hasNext();) {
////                Variable var = vars.next()
////                var.setClosureSharedVariable(true)
////            }
////            scope
////        }
//
//
//        def listenerClosure = closureX(
//                params(param(ClassHelper.make(View), "v")),
//                stmt(listener.parameters ? callThisX(listener.name, varX("v")) : callThisX(listener.name))
//        )
////        listenerClosure.variableScope = injectionMethod.variableScope.copy()
//        listenerClosure.variableScope = new VariableScope()
//
//        AstUtils.appendStatement(injectionMethod,
//                /*
//                findViewById(...).setOnClickListener { View v ->
//                    listener(v) or listener()
//                }
//                 */
////                block(
////                        shareVariables(injectionMethod.variableScope),
//                        stmt(
//                                callX(
//                                        createFindViewByIdX(listener.declaringClass, ClassHelper.make(View), id),
//                                        "setOnClickListener",
//                                        listenerClosure
//                                )
//                        )
////                )
//        )
//    }

    static Statement createListenerInjectionS(String listenerName, MethodNode listenerMethod, Expression viewId) {
        def listenerClosure = createListenerCallerClosureX(listenerName, listenerMethod)
        listenerClosure.variableScope = new VariableScope()

        /*
        (findViewById(...) as viewType).setOnClickListener(listenerClosure)
         */
        stmt(
                callX(
                        createFindViewByIdX(listenerMethod.declaringClass, ClassHelper.make(View), viewId),
                        "setOn${listenerName.capitalize()}Listener",
                        listenerClosure
                )
        )
    }

    private static ClosureExpression createListenerCallerClosureX(String listenerName, MethodNode listener) {
        switch (listenerName.capitalize()) {
            case "Click":
                closureX(
                        params(param(ClassHelper.make(View), "v")),
                        stmt(listener.parameters ? callThisX(listener.name, varX("v")) : callThisX(listener.name))
                )
                break
            case "Touch":
                Expression innerX
                switch(AstUtils.getParameterTypes(listener)) {
                    case [View, MotionEvent]:
                        innerX = callThisX(listener.name, args(varX("v"), varX("motionEvent")))
                        break
                    case [MotionEvent]:
                        innerX = callThisX(listener.name, varX("motionEvent"))
                        break
                    default:
                        throw new Exception("OnTouch listener should have [View, MotionEvent] or [MotionEvent] parameters")
                }
                closureX(
                        params(param(ClassHelper.make(View), "v"), param(ClassHelper.make(MotionEvent), "motionEvent")),
                        stmt(innerX)
                )
                break
            case "CheckedChange":
                Expression innerX
                switch(AstUtils.getParameterTypes(listener)) {
                    case [CompoundButton, boolean]:
                        innerX = callThisX(listener.name, args(varX("btn"), varX("b")))
                        break
                    case [boolean]:
                        innerX = callThisX(listener.name, varX("b"))
                        break
                    default:
                        throw new Exception("OnChange listener should have [CompoundButton, boolean] or [boolean] parameters")
                }
                closureX(
                        params(param(ClassHelper.make(CompoundButton), "btn"), param(ClassHelper.Boolean_TYPE, "b")),
                        stmt(innerX)
                )
                break
            default:
                throw new Exception("Listener not found")
        }
    }

}