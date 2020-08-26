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
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

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
    private Names names;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.trees = Trees.instance(processingEnv);
        this.scanner = new MyScanner();
        this.treeMaker = TreeMaker.instance(this.context);
        this.names = Names.instance(this.context);
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

        @Override
        public Tree visitClass(ClassTree node, Void aVoid) {
            return super.visitClass(node, aVoid);
        }

        public Tree visitMethod(MethodTree node, Void p) {
            if (node.getName().toString().equalsIgnoreCase("show")) {
                JCTree.JCVariableDecl var = makeVarDef(treeMaker.Modifiers(0)
                        , "xiao"
                        , memberAccess("java.lang.String")
                        , treeMaker.Literal("methodName"));
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) node;
                jcMethodDecl.body.stats = jcMethodDecl.body.stats.append(var);
            }
            return super.visitMethod(node, p);
        }

        public Tree visitVariable(VariableTree node, Void p) {
            if (this.getCurrentPath().getParentPath().getLeaf() instanceof ClassTree) {
                JCTree.JCVariableDecl var = makeVarDef(treeMaker.Modifiers(0)
                        , "xiao"
                        , memberAccess("java.lang.String")
                        , treeMaker.Literal("methodName"));
                JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) this.getCurrentPath().getParentPath().getLeaf();
                jcClassDecl.defs = jcClassDecl.defs.append(var);
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

        /**
         * 根据字符串获取Name，（利用Names的fromString静态方法）
         *
         * @param s
         * @return
         */
        private Name getNameFromString(String s) {
            return names.fromString(s);
        }

        /**
         * 创建变量语句
         *
         * @param modifiers
         * @param name
         * @param vartype
         * @param init
         * @return
         */
        private JCTree.JCVariableDecl makeVarDef(JCTree.JCModifiers modifiers, String name, JCTree.JCExpression vartype, JCTree.JCExpression init) {
            return treeMaker.VarDef(
                    modifiers,
                    getNameFromString(name), //名字
                    vartype, //类型
                    init //初始化语句
            );
        }

        /**
         * 创建 域/方法 的多级访问, 方法的标识只能是最后一个
         * 例如： java.lang.System.out.println
         *
         * @param components
         * @return
         */
        private JCTree.JCExpression memberAccess(String components) {
            String[] componentArray = components.split("\\.");
            JCTree.JCExpression expr = treeMaker.Ident(getNameFromString(componentArray[0]));
            for (int i = 1; i < componentArray.length; i++) {
                expr = treeMaker.Select(expr, getNameFromString(componentArray[i]));
            }
            return expr;
        }
    }
}