package com.collectz.qlombok.javac.handler;

import com.collectz.qlombok.PresenceCheck;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Modifier;

import static lombok.javac.handlers.JavacHandlerUtil.*;

@HandlerPriority(65536)
@MetaInfServices(JavacAnnotationHandler.class)
public class HandlePresenceCheck extends JavacAnnotationHandler<PresenceCheck> {


    private static final String BOOLEAN_CLASS = "java.lang.Boolean";

    @Override
    public void handle(
            final AnnotationValues<PresenceCheck> annotation,
            final JCAnnotation ast,
            final JavacNode annotationNode
    ) {

        deleteAnnotationIfNeccessary(annotationNode, PresenceCheck.class);
        handPresenceCheck(
                ast,
                annotationNode
        );
    }

    public JCExpression copyType(JavacTreeMaker treeMaker, JCVariableDecl fieldNode) {

        return fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
    }

    private void handPresenceCheck(
            JCAnnotation ast,
            JavacNode annotationNode
    ) {

        JavacNode classNode = annotationNode.up();
        for (JavacNode node : classNode.down()) {
            if (node.getKind() == Kind.FIELD && fieldQualifiesForPresenceGeneration(node)) {
                createPresenceFieldForField(node, classNode, ast);
                createPresenceGetterForField(node, classNode, ast);
            }
            if (node.getKind() == Kind.METHOD && methodQualifiesForPresenceInjection(node)) {
                injectPresenceSetForSetter(node, classNode, ast);
            }
        }
    }

    private void createPresenceFieldForField(JavacNode fieldNode, JavacNode classNode, JCAnnotation ast) {

        JCExpression booleanType = genTypeRef(fieldNode, BOOLEAN_CLASS);
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JavacTreeMaker maker = fieldNode.getTreeMaker();
        Context context = fieldNode.getContext();
        JCVariableDecl fieldDecl = recursiveSetGeneratedBy(maker.VarDef(
                maker.Modifiers(Flags.PRIVATE),
                fieldNode.toName(getPresenceFieldName(field.getName().toString())), booleanType, maker.Literal(false)
        ), ast, context);
        injectFieldAndMarkGenerated(classNode, fieldDecl);
    }

    private void createPresenceGetterForField(JavacNode fieldNode, JavacNode classNode, JCAnnotation ast) {

        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JavacTreeMaker maker = classNode.getTreeMaker();

        JCExpression methodType = genTypeRef(fieldNode, BOOLEAN_CLASS);

        String fieldName = field.getName().toString();
        String methodName = HandlerUtil.buildAccessorName("has", fieldName);
        JCTree.JCBlock methodBody = maker.Block(0, List.of(
                maker.Return(
                        maker.Select(maker.Ident(fieldNode.toName("this")), fieldNode.toName(getPresenceFieldName(fieldName)))
                )
        ));

        List<JCTree.JCTypeParameter> methodGenericParams = List.nil();
        List<JCVariableDecl> parameters = List.nil();
        List<JCExpression> throwsClauses = List.nil();
        JCExpression annotationMethodDefaultValue = null;


        JCTree.JCMethodDecl decl = recursiveSetGeneratedBy(maker.MethodDef(maker.Modifiers(Modifier.PUBLIC), fieldNode.toName(methodName), methodType,
                methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), classNode.get(), fieldNode.getContext());
        injectMethod(classNode, decl);
    }

    private void injectPresenceSetForSetter(JavacNode methodNode, JavacNode classNode, JCAnnotation ast) {

        JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) methodNode.get();
        JavacTreeMaker maker = methodNode.getTreeMaker();
        Context context = methodNode.getContext();
        String fieldName = method.getName().toString().replace("set", "");

        JCTree.JCAssign fieldPresenceAssign = maker.Assign(
                maker.Select(maker.Ident(methodNode.toName("this")), methodNode.toName(getPresenceFieldName(fieldName))),
                maker.Literal(true)
        );

        method.body = setGeneratedBy(
                maker.Block(
                        0,
                        List.of(
                                maker.Exec(fieldPresenceAssign),
                                setGeneratedBy(method.body, ast, context))
                ),
                ast, context
        );
        methodNode.rebuild();
    }

    private String getPresenceFieldName(String fieldName) {

        return "$" + fieldName.replaceAll("([^_A-Z])([A-Z])", "$1_$2").toUpperCase() + "_" + "PRESENCE";
    }

    private boolean fieldQualifiesForPresenceGeneration(JavacNode field) {

        return true;
    }

    private boolean methodQualifiesForPresenceInjection(JavacNode method) {

        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) method.get();
        return methodDecl.getName().toString().startsWith("set") && methodDecl.getName().length() > 3;
    }

}

