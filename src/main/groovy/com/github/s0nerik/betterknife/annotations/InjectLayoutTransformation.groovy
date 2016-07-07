package com.github.s0nerik.betterknife.annotations
import android.app.Activity
import android.support.v4.app.Fragment
import com.github.s0nerik.betterknife.utils.ActivityUtils
import com.github.s0nerik.betterknife.utils.AstUtils
import com.github.s0nerik.betterknife.utils.FragmentUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
final class InjectLayoutTransformation extends AbstractASTTransformation {

//    List<ClassNode> supportedClasses = [ClassHelper.make(Fragment), ClassHelper.make(Activity)]

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        init(astNodes, sourceUnit)

        def annotation = astNodes[0] as AnnotationNode
        def classNode = astNodes[1] as ClassNode

        // Layout id expression
        def id = annotation.getMember("value")

        if (AstUtils.isSubclass(classNode, Activity)) {
            injectIntoActivity(classNode, id)
        } else if (AstUtils.isSubclass(classNode, Fragment)) {
            injectIntoFragment(classNode, id)
        } else {
            addError("${classNode.name} must extend Activity or Fragment.", annotation)
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
