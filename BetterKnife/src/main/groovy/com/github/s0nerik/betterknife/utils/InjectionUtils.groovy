package com.github.s0nerik.betterknife.utils
import android.view.DragEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.TextView
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

    /**
     * Creates a single view injection line that should be added to the _injectViews() method.
     * @param fieldNode View to inject into
     * @param id Resource id
     * @return Single view injection line
     */
    private static Statement createFindViewByIdForListS(Expression rootView, VariableExpression fieldNode, ClassNode viewClass, Expression id) {
        // fieldNode.add((viewClass) rootView.findViewById(id))
        return stmt(callX(fieldNode, "add", createFindViewByIdX(rootView, viewClass, id)))
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

    static void appendFindAllViewsByIdS(Expression rootView, MethodNode methodNode, FieldNode fieldNode, List<Expression> ids) {
        def viewClass = fieldNode.type.genericsTypes ? fieldNode.type.genericsTypes[0].type : ClassHelper.make(View)
        ids.each {
            AstUtils.appendStatement(methodNode, createFindViewByIdForListS(rootView, varX(fieldNode), viewClass, it))
        }
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
        (rootView.findViewById(...) as View).setOnClickListener(listenerClosure)
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
        def createArgsFor = { List<Class> classes ->
            def argExprs = classes.collect { varX("_${it.simpleName.toLowerCase()}", ClassHelper.make(it)) }
            args(argExprs as List<Expression>)
        }

        def createParamsFor = { List<Class> classes ->
            def paramExprs = classes.collect { param(ClassHelper.make(it.name), "_${it.simpleName.toLowerCase()}") }
            params(paramExprs as Parameter[])
        }

        println "paramTypes: ${AstUtils.getParameterTypes(listener)}"

        Expression innerX = callThisX listener.name, createArgsFor(AstUtils.getParameterTypes(listener))
        Parameter[] closureParams = []

        switch (listenerName.capitalize()) {
            case "Click":
            case "LongClick":
                closureParams = createParamsFor([View])
                break
            case "ItemClick":
                closureParams = createParamsFor([AdapterView, View, int, long])
                break
            case "Touch":
                closureParams = createParamsFor([View, MotionEvent])
                break
            case "EditorAction":
                closureParams = createParamsFor([TextView, int, KeyEvent])
                break
            case "CheckedChange":
                closureParams = createParamsFor([CompoundButton, boolean])
                break
            case "FocusChange":
                closureParams = createParamsFor([View, boolean])
                break
            case "Drag":
                closureParams = createParamsFor([View, DragEvent])
                break
            default:
                throw new Exception("Listener not found")
        }

        closureX(closureParams, stmt(innerX))
    }

}