package com.github.s0nerik.betterknife.utils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
final class FragmentUtils {

    /**
     * Creates onCreateView method with specified layout injected.
     * @param id ID of layout to inflate.
     * @return onCreateView method with inject layout.
     */
    static MethodNode createOnCreateViewMethod(Expression id) {
        def node = AstUtils.createMethod(
                "onCreateView",
                [
                        returnType: ClassHelper.make(View),
                        params: params(
                                        param(ClassHelper.make(LayoutInflater), "inflater"),
                                        param(ClassHelper.make(ViewGroup), "container"),
                                        param(ClassHelper.make(Bundle), "savedInstanceState")
                        ),
                        code      : block(
                                // super.onCreateView(inflater, container, savedInstanceState)
                                AstUtils.createSuperCallStatement(
                                        "onCreateView",
                                        args(
                                                varX("inflater", ClassHelper.make(LayoutInflater)),
                                                varX("container", ClassHelper.make(ViewGroup)),
                                                varX("savedInstanceState", ClassHelper.make(Bundle))
                                        )
                                ),
                                // View v = inflater.inflate(id, container, false)
                                declS(
                                        varX("v", ClassHelper.make(View)),
                                        callX(
                                                varX("inflater", ClassHelper.make(LayoutInflater)),
                                                "inflate",
                                                args(id, varX("container", ClassHelper.make(ViewGroup)), constX(false, true))
                                        )
                                ),
                                // return v
                                returnS(varX("v", ClassHelper.make(View)))
                        )
                ]
        )

        node.addAnnotation(new AnnotationNode(ClassHelper.make(Override)))

        return node
    }

    /**
     *
     * @return Empty onViewCreated method.
     */
    static MethodNode createOnViewCreatedMethod() {
        def node = AstUtils.createMethod(
                "onViewCreated",
                [
                        params: params(
                                    param(ClassHelper.make(View), "view"),
                                    param(ClassHelper.make(Bundle), "savedInstanceState")
                        ),
                        code  : block(
                                    // super.onCreateView(view, savedInstanceState)
                                    AstUtils.createSuperCallStatement("onViewCreated", args(varX("view", ClassHelper.make(View)), varX("savedInstanceState", ClassHelper.make(Bundle))))
                        )
                ]
        )

        node.addAnnotation(new AnnotationNode(ClassHelper.make(Override)))

        return node
    }

    /**
     *
     * @param code Code block
     * @param viewName Name of view to search for
     * @return Position of "def v = inflater.inflate(...)"
     */
    static int findViewInflationLineNumber(BlockStatement code, String viewName) {
        code.statements.findIndexOf { Statement it ->
            if (it instanceof ExpressionStatement) {
                def expr = it as ExpressionStatement
                if (expr instanceof BinaryExpression) {
                    def binExpr = expr as BinaryExpression
                    if (binExpr.leftExpression instanceof VariableExpression) {
                        def viewVar = binExpr.leftExpression as VariableExpression
                        return viewVar.name == viewName
                    }
                }
            }
            return false
        }
    }

    static void injectViews(ClassNode fragmentClass) {
        MethodNode onViewCreatedMethod = AstUtils.findMethod(fragmentClass, "onViewCreated", 2)
        if (!onViewCreatedMethod) {
            onViewCreatedMethod = createOnViewCreatedMethod()
            fragmentClass.addMethod(onViewCreatedMethod)
        }

        if (onViewCreatedMethod.code instanceof BlockStatement) {
            def onViewCreatedMethodStatements = AstUtils.getMethodStatements(onViewCreatedMethod)

            def injectCall = InjectionUtils.createInjectViewsCall(callThisX("getView"))

            if (!onViewCreatedMethodStatements.find { it?.statementLabel == injectCall.statementLabel }) {
                def pos = 0
                if (onViewCreatedMethod.firstStatement?.statementLabels?.find {it == AstUtils.SUPER_CALL}) {
                    pos = 1
                }
                onViewCreatedMethodStatements.add(pos, injectCall)
            }
        }

    }

}