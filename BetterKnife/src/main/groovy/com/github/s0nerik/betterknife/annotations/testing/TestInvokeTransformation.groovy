package com.github.s0nerik.betterknife.annotations.testing

import com.github.s0nerik.betterknife.utils.AstUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.ClassHelper.Boolean_TYPE
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
class TestInvokeTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        init(astNodes, sourceUnit)

        def annotationNode = astNodes[0] as AnnotationNode
        def methodNode = astNodes[1] as MethodNode
        ClassNode declaringClass = methodNode.declaringClass

        def field = new FieldNode("${methodNode.name}Invoked", ACC_PUBLIC | ACC_SYNTHETIC, Boolean_TYPE, declaringClass, constX(false))
        declaringClass.addField field

        if (methodNode.code instanceof BlockStatement) {
            AstUtils.appendStatement methodNode, assignS(varX(field), constX(true))
        } else if (methodNode.code instanceof ReturnStatement) {
            methodNode.code = block assignS(varX(field), constX(true)), methodNode.code
        }
    }
}
