package com.github.s0nerik.betterknife.annotations
import android.os.Bundle
import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
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
        def node = AstUtils.createMethod "onCreate", [modifiers: Opcodes.ACC_PROTECTED,
                                                      params   : params(param(ClassHelper.make(Bundle), "savedInstanceState")),
                                                      code     : block(createSuperOnCreateCall("savedInstanceState"))]

        node.addAnnotation(new AnnotationNode(ClassHelper.make(Override)))

        return node
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

    static void appendFindViewByIdStatement(MethodNode methodNode, FieldNode fieldNode, Expression id) {
        AstUtils.appendStatement(methodNode, createInjectLine(fieldNode, id))
    }

    static Statement createInjectLine(FieldNode fieldNode, Expression id) {
        // field = (field.type) findViewById(id)
        return assignS(varX(fieldNode), castX(fieldNode.type, callThisX('findViewById', id)))
    }

    static Expression createGetIdentifier(String id) {
        // resources.getIdentifier("viewName", "id", getPackageName())
        callX(varX("resources"), "getIdentifier", args(constX(id), constX("id"), callThisX("getPackageName")))
    }

    static void injectViews(ClassNode activityClass) throws Exception {
        def onCreateMethod = activityClass.methods.find {it.name == "onCreate" && it.parameters.size() == 1}
        if (!onCreateMethod) {
            onCreateMethod = createOnCreateMethod()
            activityClass.addMethod(onCreateMethod)
        }

        if (onCreateMethod.code instanceof BlockStatement) {
            def onCreateMethodStatements = AstUtils.getMethodStatements(onCreateMethod)

            def injectCall = InjectionUtils.createInjectViewsCall()

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
                onCreateMethodStatements.add(injectionIndex, injectCall)
            }
        }

    }


}