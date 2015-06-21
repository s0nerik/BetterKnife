package com.github.s0nerik.betterknife.annotations

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@CompileStatic
final class AstUtils {

    /**
     * Creates a super call for given method name and arguments.
     * @param methodName
     * @param arguments
     * @return
     */
    static Statement createSuperCallStatement(String methodName, Expression arguments = null) {
        // super.method(arguments)
        return stmt(arguments ? callSuperX(methodName, arguments) : callSuperX(methodName))
    }

    /**
     * Creates method with a label assigned to it.
     * @param methodName
     * @param arguments
     * @param label
     * @return
     */
    static Statement createMethodCallStatementWithLabel(String methodName,
                                                        ArgumentListExpression arguments = null,
                                                        String label = methodName) {
        // this.method(arguments)
        def s = stmt(arguments ? callThisX(methodName, arguments) : callThisX(methodName))
        s.statementLabel = label
        return s
    }

    /**
     * Creates MethodNode with given parameters. Default parameters are "public void method(){}".
     * <p>Arguments:</p>
     * <p><b>modifiers</b>:     Access modifiers, separated by "|"</p>
     * <p><b>returns</b>:       Return type</p>
     * <p><b>params</b>:        Parameters list</p>
     * <p><b>exceptions</b>:    Exceptions list</p>
     * <p><b>code</b>:          Method code</p>
     * @param methodName Method name
     * @param args Additional arguments
     * @return New MethodNode with specified parameters
     */
    static MethodNode createMethod(String name, Map args = [:]) {
        return new MethodNode(
                name,
                (args.modifiers ?: Opcodes.ACC_PUBLIC) as int,
                (args.returns ?: ClassHelper.VOID_TYPE) as ClassNode,
                (args.params ?: params()) as Parameter[],
                (args.exceptions ?: ClassNode.EMPTY_ARRAY) as ClassNode[],
                (args.code ?: block()) as BlockStatement
        )
    }

    /** Look for method with a given object and name within a block statement.
     * @param blockStatement Block statement
     * @param object Method call object
     * @param methodName Method name
     * @return Found method position in block, -1 if not found
     */
    public static int findMethodCallPosition(BlockStatement blockStatement, String object, String methodName) {

        int position = -1

        int index = 0
        for (Statement statement : blockStatement.statements) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = statement as ExpressionStatement
                if (expressionStatement.expression instanceof MethodCallExpression) {
                    MethodCallExpression methodCallExpression = expressionStatement.expression as MethodCallExpression
                    if (methodCallExpression.objectExpression.text == object && methodCallExpression.methodAsString == methodName) {
                        position = index
                        break
                    }
                }
            }
            index++
        }

        return position
    }

    public static int findSetterCallPosition(BlockStatement blockStatement, String propertyName) {
        String propertySetterName = "set" + propertyName.capitalize()

        return blockStatement.statements.findIndexOf { Statement it ->
            if (it instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = it as ExpressionStatement

                if (expressionStatement.expression instanceof MethodCallExpression) {
                    MethodCallExpression methodCallExpression = expressionStatement.expression as MethodCallExpression
                    if (methodCallExpression.methodAsString == propertySetterName) {
                        return true
                    }
                }

                if (expressionStatement.expression instanceof BinaryExpression) {
                    def binaryExpression = expressionStatement.expression as BinaryExpression
                    if (binaryExpression.leftExpression instanceof VariableExpression) {
                        VariableExpression variableExpression = binaryExpression.leftExpression as VariableExpression
                        if (variableExpression.text == propertyName) {
                            return true
                        }
                    }
                }
            }
            return false
        }
    }

    @Memoized
    static boolean isSubclass(ClassNode original, Class compared) {
        return original.isDerivedFrom(ClassHelper.make(compared))
    }

    /**
     *
     * @param method
     * @return Statements of the method.
     */
    static List<Statement> getMethodStatements(MethodNode method) {
        return (method.code as BlockStatement).statements
    }

    /**
     * Adds a new line with specified statement.
     * @param method
     * @param statement
     */
    static void appendStatement(MethodNode method, Statement statement) {
        getMethodStatements(method) << statement
    }

    /**
     * Look for return statement position in a block statment
     * @param blockStatement Block statement
     * @return Found method position in block, -1 if not found
     */
    @Memoized
    static boolean superClassHasAnnotation(ClassNode thisClass, AnnotationNode annotationNode) {
        def c = thisClass.superClass
        while (c) {
            if (c.annotations.contains(annotationNode)) {
                return true
            }
            c = c.superClass
        }
        return false
    }

    @Memoized
    static boolean superClassHasFieldWithAnnotation(ClassNode thisClass, ClassNode annotationClass) {
        def c = thisClass.superClass
        while (c) {
            if (classHasFieldWithAnnotation(c, annotationClass)) {
                return true
            }
            c = c.superClass
        }
        return false
    }

    static boolean classHasFieldWithAnnotation(ClassNode thisClass, ClassNode annotationClass) {
        if (thisClass.fields.find { it.annotations.find {it.classNode == annotationClass} }) {
            return true
        }
        return false
    }

    /**
     *
     * @param thisClass
     * @param superClass
     * @return All fields that are subclasses of specified superClass.
     */
    static List<FieldNode> getAllSubtypeFields(ClassNode thisClass, Class superClass) {
        return thisClass.fields.findAll { isSubclass(it.type, superClass) }
    }

    /**
     * Look for return statement position in a block statment
     * @param blockStatement Block statement
     * @return Found method position in block, -1 if not found
     */
    static int findReturnPosition(BlockStatement blockStatement) {

        int position = -1

        int index = 0
        for (Statement statement : blockStatement.statements) {
            if (statement instanceof ReturnStatement) {
                position = index
                break
            }
            index++
        }

        return position
    }

    static MethodNode findMethod(ClassNode classNode, String name, int paramsNum) {
        return classNode.methods.find { it.name == name && it.parameters.size() == paramsNum }
    }

    static Statement findStatementByLabel(List<Statement> statements, String label) {
        statements.find { it?.statementLabel == label }
    }

}