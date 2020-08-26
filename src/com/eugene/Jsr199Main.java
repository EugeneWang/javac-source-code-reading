package com.eugene;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Jsr199Main {

    public static void main(String[] args) throws URISyntaxException, IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        VisitTreeProcessor processor = new VisitTreeProcessor();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        File file = new File("src/com/eugene/HelloWorld.java");
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(file));

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, Arrays.asList("-d", "src/com/eugene"), null, compilationUnits);
        task.setProcessors(Arrays.asList(processor));
        task.call();

//        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
//            System.out.format("Error on line %d in %s\n%s\n",
//                    diagnostic.getLineNumber(), diagnostic.getSource().toUri(), diagnostic.getMessage(null));
//        }

        fileManager.close();
    }
}