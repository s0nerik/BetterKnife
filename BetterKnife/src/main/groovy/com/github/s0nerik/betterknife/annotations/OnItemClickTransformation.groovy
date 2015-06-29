package com.github.s0nerik.betterknife.annotations

import android.widget.AdapterView
import com.github.s0nerik.betterknife.BetterKnife
import com.github.s0nerik.betterknife.utils.AnnotationUtils
import com.github.s0nerik.betterknife.utils.InjectionUtils
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * Created by Arasthel on 16/08/14.
 */

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class OnItemClickTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        MethodNode annotatedMethod = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedMethod.declaringClass;

        MethodNode injectMethod = InjectionUtils.getInjectViewsMethod(declaringClass);

        def ids = [];

        if (annotation.members.size() > 0) {
            if (annotation.members.value instanceof ListExpression) {
                annotation.members.value.getExpressions().each {
                    ids << (String) it.property.getValue();
                };
            }
            else {
                ids << (String) annotation.members.value.property.getValue();
            }
        }
        else {
            throw new Exception("OnItemClick must have an id");
        }

        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements();

        ids.each { String id ->
            Statement statement = createInjectStatement(id, annotatedMethod, injectMethod);
            statementList.add(statement);
        }
    }

    private Statement createInjectStatement(String id, MethodNode method, MethodNode injectMethod) {

        Parameter viewParameter = injectMethod.parameters.first()

        Variable variable = varX("v", ClassHelper.make(AdapterView))

        BlockStatement statement =
                block(
                        AnnotationUtils.createInjectExpression(variable, viewParameter, id),
                        stmt(callX(ClassHelper.make(BetterKnife), "setOnItemClick",
                                args(varX(variable), varX("this"), constX(method.name))))
                )

        return statement;

    }
}
