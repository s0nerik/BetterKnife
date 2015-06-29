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
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@CompileStatic
final class InjectViewTransformation extends AbstractASTTransformation {

    private static final List<Class> SUPPORTED_CLASSES = [Activity, Fragment]

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        init(astNodes, sourceUnit)

        def annotationNode = astNodes[0] as AnnotationNode
        def annotatedNode = astNodes[1] as AnnotatedNode
        def declaringClass = annotatedNode.declaringClass

        if (annotatedNode instanceof FieldNode) {
            def fieldNode = annotatedNode as FieldNode
            if (!AstUtils.isSubclass(fieldNode.type, View)) {
                addError("Annotated field must extend View class. Type: ${fieldNode.type.name}", fieldNode)
                return
            }
        }

        if (!SUPPORTED_CLASSES.find {AstUtils.isSubclass(declaringClass, it)}) {
            addError("@${annotationNode.classNode.name} can only be applied to fields or methods of ${SUPPORTED_CLASSES}.", annotationNode)
            return
        }

        // We don't need to do anything if we already have _injectViews() method
        if (InjectionUtils.hasInjectViewsMethod(declaringClass)) return

        // Create _injectViews() method and assign all views to their values inside it
        def injectMethod = InjectionUtils.getOrCreateInjectViewsMethod declaringClass
        injectAllAnnotatedViewsIntoMethod(injectMethod)
        injectAllAnnotatedListenersIntoMethod(injectMethod)

        // Actually call _injectViews() method in the proper place
        if (AstUtils.isSubclass(declaringClass, Activity)) { // Activity case
            try {
                ActivityUtils.injectViews(declaringClass)
            } catch (Exception e) {
                addError(e.message, declaringClass)
            }
        } else if (AstUtils.isSubclass(declaringClass, Fragment)) { // Fragment case
            FragmentUtils.injectViews(declaringClass)
        }
    }

    static void injectAllAnnotatedViewsIntoMethod(MethodNode injectMethod) {
        def views = AstUtils.getAllAnnotatedFields(injectMethod.declaringClass, ClassHelper.make(InjectView))

        views.each {
            // View id expression
            def idExpression = AstUtils.getAnnotationMember(it, InjectView, "value")

            if (idExpression) {
                InjectionUtils.appendFindViewByIdStatement(injectMethod, it, idExpression)
            } else {
                InjectionUtils.appendFindViewByIdStatement(injectMethod, it, it.name)
            }
        }
    }

    static void injectAllAnnotatedListenersIntoMethod(MethodNode injectMethod) {

        // Inject OnClick listeners
        def clickListeners = AstUtils.getAllAnnotatedMethods(injectMethod.declaringClass, ClassHelper.make(OnClick))
        clickListeners.each { MethodNode listener ->

            // View id expression
            def idExpression = AstUtils.getAnnotationMember(listener, OnClick, "value")

            if (idExpression instanceof ListExpression) {
                def idExpressionList = idExpression as ListExpression
                idExpressionList.expressions.each { Expression id ->
                    AstUtils.appendStatement(injectMethod, InjectionUtils.createListenerInjectionS("Click", listener, id))
                }
            } else {
                AstUtils.appendStatement(injectMethod, InjectionUtils.createListenerInjectionS("Click", listener, idExpression))
//                InjectionUtils.appendOnClickListenerStatement(injectMethod, listener, idExpression)
            }
        }

    }

}
