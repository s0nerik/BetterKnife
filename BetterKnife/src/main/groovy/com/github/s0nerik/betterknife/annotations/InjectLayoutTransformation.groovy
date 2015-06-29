package com.github.s0nerik.betterknife.annotations
import android.app.Activity
import android.support.v4.app.Fragment
import android.view.View
import com.github.s0nerik.betterknife.utils.ActivityUtils
import com.github.s0nerik.betterknife.utils.AstUtils
import com.github.s0nerik.betterknife.utils.FragmentUtils
import com.github.s0nerik.betterknife.utils.InjectionUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
final class InjectLayoutTransformation extends AbstractASTTransformation {

//    List<ClassNode> supportedClasses = [ClassHelper.make(Fragment), ClassHelper.make(Activity)]

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        init(astNodes, sourceUnit)
        if (!astNodes) return
        if (!astNodes[0] || !astNodes[1]) return
        if (!(astNodes[0] instanceof AnnotationNode)) return
        if (!(astNodes[1] instanceof ClassNode)) return

        def annotation = astNodes[0] as AnnotationNode
        def classNode = astNodes[1] as ClassNode

        // Layout id expression
        def id = annotation.getMember("value")
        def injectAllViews = (annotation.getMember("injectAllViews") as ConstantExpression)?.value as boolean

        List<FieldNode> views = null
        MethodNode injectViewsMethod = null
        if (injectAllViews) {
            views = AstUtils.getAllSubtypeFields(classNode, View)
            injectViewsMethod = InjectionUtils.getOrCreateInjectViewsMethod classNode
        }

        if (AstUtils.isSubclass(classNode, Activity)) {
            injectIntoActivity(classNode, id)

            if (injectAllViews) {
                views?.each {
                    InjectionUtils.appendFindViewByIdStatement(injectViewsMethod, it, InjectionUtils.createGetIdentifier(classNode, it.name))
                }
            }
        } else if (AstUtils.isSubclass(classNode, Fragment)) {
            injectIntoFragment(classNode, id)

            if (injectAllViews) {
                views?.each {
                    InjectionUtils.appendFindViewByIdStatement(injectViewsMethod, it, InjectionUtils.createGetIdentifier(classNode, it.name))
                }
                FragmentUtils.injectViews(classNode)
            }
        } else {
            addError("${classNode.name} must extend Activity or Fragment.", classNode)
        }

    }

    void injectIntoFragment(ClassNode classNode, Expression id) {
        MethodNode onCreateViewMethod
        if (!hasDeclaredMethod(classNode, "onCreateView", 3)) {
            onCreateViewMethod = FragmentUtils.createOnCreateViewMethod(id)
            classNode.addMethod(onCreateViewMethod)
        } else {
            addError('''You can't use onCreateView() method with @InjectLayout.
                        You can safely move all your code from onCreateView() to onViewCreated()'''.stripIndent(), classNode)
        }
    }

    static void injectIntoActivity(ClassNode classNode, Expression id) {
        MethodNode onCreateMethod
        if (!hasDeclaredMethod(classNode, "onCreate", 1)) {
            onCreateMethod = ActivityUtils.createOnCreateMethod()
            classNode.addMethod(onCreateMethod)
        } else {
            onCreateMethod = classNode.getMethods("onCreate").first()
        }

        def onCreateMethodStatements = (onCreateMethod.code as BlockStatement).statements
        def superCallIndex = AstUtils.findMethodCallPosition(onCreateMethod.code as BlockStatement, "super", "onCreate")
        if (superCallIndex < 0) {
            onCreateMethodStatements.add(0, ActivityUtils.createSuperOnCreateCall(onCreateMethod.parameters[0].name))
        }

        onCreateMethodStatements.add(1, ActivityUtils.createSetContentView(id))
    }
}
