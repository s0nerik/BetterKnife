package com.github.s0nerik.betterknife.utils

import android.view.DragEvent
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
    static Statement createInjectViewsCall(Expression view) {
        return AstUtils.createMethodCallStatementWithLabel("_injectViews", view)
    }

    static boolean hasInjectViewsMethod(ClassNode classNode) {
        return AstUtils.findDeclaredMethod(classNode, "_injectViews", params(param(ClassHelper.make(View), "view"))) as boolean
    }

    static MethodNode getOrCreateInjectViewsMethod(ClassNode classNode) {
        MethodNode injectMethod = AstUtils.findDeclaredMethod(classNode, "_injectViews", params(param(ClassHelper.make(View), "view")))

        if (!injectMethod) {
            injectMethod = AstUtils.createMethod "_injectViews", [params: param(ClassHelper.make(View), "view")]
            if (AstUtils.superClassHasFieldWithAnnotation(classNode, InjectView)) {
                // super._injectViews(view)
                AstUtils.appendStatement(injectMethod, AstUtils.createSuperCallStatement("_injectViews", varX("view", ClassHelper.make(View))))
            }
            classNode.addMethod injectMethod
        }

        return injectMethod
    }

    /**
     *
     * @param id
     * @return context.getResources().getIdentifier(id, "id", context.getPackageName())
     */
    static Expression createGetIdentifierX(Expression context, String id) {
        Expression getPackageNameX = callX(context, "getPackageName")
        return callX(callX(context, "getResources"), "getIdentifier", args(constX(id), constX("id"), getPackageNameX))
    }

    /**
     * Creates a single view injection line that should be added to the _injectViews() method.
     * @param fieldNode View to inject into
     * @param id Resource id
     * @return Single view injection line
     */
    private static Statement createFindViewByIdS(Expression rootView, VariableExpression fieldNode, Expression id) {
        // fieldNode = (fieldNode.type) rootView.findViewById(id)
        return assignS(fieldNode, createFindViewByIdX(rootView, fieldNode.type, id))
    }

    private static Expression createFindViewByIdX(Expression rootView, ClassNode viewClass, Expression id) {
        // (viewClass) rootView.findViewById(id)
        return castX(viewClass, callX(rootView, 'findViewById', id))
    }

    /**
     * Adds a single view injection line into _injectViews() method.
     * @param methodNode Method where the statement should be appended
     * @param fieldNode View to inject into
     * @param id Resource id
     */
    static void appendFindViewByIdS(Expression rootView, MethodNode methodNode, FieldNode fieldNode, Expression id) {
        AstUtils.appendStatement(methodNode, createFindViewByIdS(rootView, varX(fieldNode), id))
    }

    /**
     * Adds a single view injection line into _injectViews() method.
     * @param methodNode Method where the statement should be appended
     * @param fieldNode View to inject into
     * @param id Resource id as a String
     */
    static void appendFindViewByIdS(Expression rootView, MethodNode methodNode, FieldNode fieldNode, String idString) {
        def findLowerCased = createFindViewByIdS(rootView, varX(fieldNode), createGetIdentifierX(callX(rootView, "getContext"), AnnotationUtils.camelCaseToLowerCase(idString)))
        def findOriginal = createFindViewByIdS(rootView, varX(fieldNode), createGetIdentifierX(callX(rootView, "getContext"), idString))

        AstUtils.appendStatements(methodNode, [findLowerCased, ifS(equalsNullX(varX(fieldNode)), findOriginal)])
    }

    static Statement createListenerInjectionS(Expression rootView, String listenerName, MethodNode listenerMethod, Expression viewId) {
        def listenerClosure = createListenerCallerClosureX(listenerName, listenerMethod)
        listenerClosure.variableScope = new VariableScope()

        /*
        (rootView.findViewById(...) as viewType).setOnClickListener(listenerClosure)
         */
        stmt(
                callX(
                        createFindViewByIdX(rootView, ClassHelper.make(View), viewId),
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
            case "LongClick":
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
            case "FocusChange":
                Expression innerX
                switch(AstUtils.getParameterTypes(listener)) {
                    case [View, boolean]:
                        innerX = callThisX(listener.name, args(varX("v"), varX("b")))
                        break
                    case [boolean]:
                        innerX = callThisX(listener.name, varX("b"))
                        break
                    default:
                        throw new Exception("OnChange listener should have [View, boolean] or [boolean] parameters")
                }
                closureX(
                        params(param(ClassHelper.make(View), "v"), param(ClassHelper.Boolean_TYPE, "b")),
                        stmt(innerX)
                )
                break
            case "Drag":
                Expression innerX
                switch(AstUtils.getParameterTypes(listener)) {
                    case [View, DragEvent]:
                        innerX = callThisX(listener.name, args(varX("v"), varX("b")))
                        break
                    case [DragEvent]:
                        innerX = callThisX(listener.name, varX("b"))
                        break
                    default:
                        throw new Exception("OnChange listener should have [View, DragEvent] or [DragEvent] parameters")
                }
                closureX(
                        params(param(ClassHelper.make(View), "v"), param(ClassHelper.make(DragEvent), "b")),
                        stmt(innerX)
                )
                break
            default:
                throw new Exception("Listener not found")
        }
    }

}