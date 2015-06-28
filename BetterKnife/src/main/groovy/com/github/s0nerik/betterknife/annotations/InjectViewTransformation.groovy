package com.github.s0nerik.betterknife.annotations
import android.app.Activity
import android.support.v4.app.Fragment
import android.view.View
import com.github.s0nerik.betterknife.utils.ActivityUtils
import com.github.s0nerik.betterknife.utils.AstUtils
import com.github.s0nerik.betterknife.utils.FragmentUtils
import com.github.s0nerik.betterknife.utils.InjectionUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@CompileStatic
final class InjectViewTransformation extends AbstractASTTransformation {

    private static final List<Class> SUPPORTED_CLASSES = [Activity, Fragment]

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        init(astNodes, sourceUnit)
        if (!astNodes) return
        if (!astNodes[0] || !astNodes[1]) return
        if (!(astNodes[0] instanceof AnnotationNode)) return
        if (!(astNodes[1] instanceof FieldNode)) return

        def annotation = astNodes[0] as AnnotationNode
        def fieldNode = astNodes[1] as FieldNode
        def declaringClass = fieldNode.declaringClass

        if (!AstUtils.isSubclass(fieldNode.type, View)) {
            addError("Annotated field must extend View class. Type: ${fieldNode.type.name}", fieldNode)
            return
        }

        // View id expression
        def id = annotation.getMember "value" ?: fieldNode.name

        if (!SUPPORTED_CLASSES.find {AstUtils.isSubclass(declaringClass, it)}) {
            addError("@InjectView can only be applied to the fields of Activity or Fragment.", declaringClass)
            return
        }

        def injectMethod = InjectionUtils.getInjectViewsMethod declaringClass

        // Activity case
        if (AstUtils.isSubclass(declaringClass, Activity)) {
            InjectionUtils.appendFindViewByIdStatement(injectMethod, fieldNode, id)
            try {
                ActivityUtils.injectViews(declaringClass)
            } catch (Exception e) {
                addError(e.message, declaringClass)
            }
        } else if (AstUtils.isSubclass(declaringClass, Fragment)) {
            InjectionUtils.appendFindViewByIdStatement(injectMethod, fieldNode, id)
            FragmentUtils.injectViews(declaringClass)
        }
    }

}
