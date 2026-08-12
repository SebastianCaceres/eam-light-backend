package ch.cern.eam.refactor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to automate the migration of ServiceImpl classes from SOAP to JPA.
 * This skeleton sets up JavaParser with Symbol Solving to correctly identify
 * InforWebServicesPT method calls and replace them with JPA repository calls.
 */
public class JpaMigrationRefactorer {

    private static final String SRC_DIR = "../eam-wshub-core/src/main/java";

    public static void main(String[] args) throws IOException {
        // 1. Set up Type Solver to resolve types
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        // 2. Find all ServiceImpl files
        Path coreSrcPath = Paths.get(SRC_DIR);
        List<Path> serviceImplFiles;
        try (Stream<Path> paths = Files.walk(coreSrcPath)) {
            serviceImplFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("ServiceImpl.java"))
                    .collect(Collectors.toList());
        }

        System.out.println("Found " + serviceImplFiles.size() + " ServiceImpl files to analyze.");

        // 3. Process each file
        for (Path file : serviceImplFiles) {
            processFile(file.toFile());
        }
    }

    private static void processFile(File file) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(file);

        cu.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(classDecl -> {
            List<String> repoFields = classDecl.getFields().stream()
                    .map(f -> f.getVariable(0))
                    .filter(v -> v.getTypeAsString().endsWith("Repository"))
                    .map(v -> v.getTypeAsString() + " " + v.getNameAsString())
                    .collect(Collectors.toList());

            if (!repoFields.isEmpty()) {
                System.out.println("\n[MIGRATABLE] " + classDecl.getNameAsString());
                System.out.println("  Repositories: " + repoFields);

                cu.accept(new ModifierVisitor<Void>() {
                    @Override
                    public Visitable visit(MethodDeclaration md, Void arg) {
                        super.visit(md, arg);

                        List<MethodCallExpr> methodCalls = md.findAll(MethodCallExpr.class);
                        boolean callsSoap = methodCalls.stream()
                                .anyMatch(mc -> mc.getNameAsString().equals("performInforOperation"));

                        if (callsSoap) {
                            System.out.println("  - SOAP Method: " + md.getNameAsString());
                        }

                        return md;
                    }
                }, null);
            }
        });
    }
}
