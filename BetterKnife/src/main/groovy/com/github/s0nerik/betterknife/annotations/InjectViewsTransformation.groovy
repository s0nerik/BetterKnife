package com.github.s0nerik.betterknife.annotations

import com.github.s0nerik.betterknife.utils.AnnotationUtils
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
public class InjectViewsTransformation implements ASTTransformation, Opcodes {

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        FieldNode annotatedField = astNodes[1];
        AnnotationNode annotation = astNodes[0];
        ClassNode declaringClass = annotatedField.declaringClass;

        MethodNode injectMethod = AnnotationUtils.getInjectViewsMethod(declaringClass);

        Parameter viewParameter = injectMethod.parameters.first()

        def ids = [];

        ClassNode fieldClass = annotatedField.getType();

        if (!AnnotationUtils.doesClassImplementInterface(fieldClass, List.class)) {
            throw new Exception("The annotated field must implement or be a List. Type: ${fieldClass.name}");
        }

        if (annotation.members.size() > 0) {
            if (annotation.members.value instanceof ListExpression) {
                annotation.members.value.getExpressions().each {
                    ids << it.property.getValue() as String;
                };
            }
            else {
                throw new Exception("InjectViews must have a list of ids");
            }
        }
        else {
            throw new Exception("InjectViews must have a list of ids");
        }


        List<Statement> statementList = ((BlockStatement) injectMethod.getCode()).getStatements();

        statementList.add(createViewListStatement(annotatedField));

        ids.each { String id -> statementList.add(createInjectViewStatement(annotatedField,
                viewParameter, id)); }

    }

    private Statement createViewListStatement(FieldNode field) {
        return assignS(varX(field), ctorX(ClassHelper.make(ArrayList)))
    }

    private Statement createInjectViewStatement(FieldNode field, Parameter viewParameter,
                                                String id) {
        return AnnotationUtils.createListInjectExpression(field, viewParameter, id)
    }
}
