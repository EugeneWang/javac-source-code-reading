package com.eugene;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("*")
public class VisitTreeProcessor extends AbstractProcessor {
    private Trees trees;
    private MyScanner scanner;
    private TreeMaker treeMaker;
    private Context context;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.trees = Trees.instance(processingEnv);
        this.scanner = new MyScanner();
        this.treeMaker = TreeMaker.instance(this.context);
    }

    public boolean process(Set<? extends TypeElement> types, RoundEnvironment environment) {
        if (!environment.processingOver()) {
            for (Element element : environment.getRootElements()) {
                TreePath path = trees.getPath(element);
                scanner.scan(path, null);
            }
        }
        return true;
    }

    public class MyScanner extends TreePathScanner<Tree, Void> {

        public Tree visitMethod(MethodTree node, Void p) {
            System.out.println("方法 " + node.getKind() + ": " + node.getName());
            return super.visitMethod(node, p);
        }

        public Tree visitVariable(VariableTree node, Void p) {
            if (this.getCurrentPath().getParentPath().getLeaf() instanceof ClassTree) {
                System.out.println("字段 " + node.getKind() + ": " + node.getName());
            }
            return super.visitVariable(node, p);
        }

        @Override
        public Tree visitMethodInvocation(MethodInvocationTree node, Void aVoid) {
            JCTree.JCMethodInvocation jcMethodInvocationTree = (JCTree.JCMethodInvocation) node;
            if (jcMethodInvocationTree.meth instanceof JCTree.JCFieldAccess) {
                JCTree.JCFieldAccess meth = (JCTree.JCFieldAccess) jcMethodInvocationTree.meth;
                if (meth.name.toString().equalsIgnoreCase("println")) {
                    JCTree.JCLiteral lt = (JCTree.JCLiteral) jcMethodInvocationTree.getArguments().get(0);
                    JCTree.JCLiteral rt = treeMaker.Literal(TypeTag.CLASS, ". add JCLiteral.");
                    JCTree.JCBinary binary = treeMaker.Binary(JCTree.Tag.PLUS, lt, rt);
                    jcMethodInvocationTree.args = List.<JCTree.JCExpression>of(binary);
                }
            }



//            for (ExpressionTree item : node.getArguments()) {
//                if (item instanceof JCTree.JCLiteral) {
//                    JCTree.JCLiteral lt = (JCTree.JCLiteral) item;
//                    //lt.value = lt.value + " world";
//                    System.out.println(lt.getValue());
//                }
//            }


            return super.visitMethodInvocation(node, aVoid);
        }

        @Override
        public Tree visitAssignment(AssignmentTree node, Void aVoid) {
            return super.visitAssignment(node, aVoid);
        }
    }
}