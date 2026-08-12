package ch.cern.eam.refactor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class SoapEradicator {

    private static final String SRC_DIR = "eam-wshub-core/src/main/java";

    public static void main(String[] args) throws Exception {
        Path rootPath = Paths.get(SRC_DIR);
        if (!Files.exists(rootPath)) {
            System.err.println("Source directory not found: " + rootPath.toAbsolutePath());
            return;
        }

        List<Path> serviceImplFiles = Files.walk(rootPath)
                .filter(p -> p.toString().endsWith("ServiceImpl.java"))
                .collect(Collectors.toList());

        System.out.println("Found " + serviceImplFiles.size() + " ServiceImpl files to optimize.");

        int filesModified = 0;
        for (Path file : serviceImplFiles) {
            if (processFile(file)) {
                filesModified++;
            }
        }

        System.out.println("Finished! Optimized " + filesModified + " files.");
    }

    private static boolean processFile(Path filePath) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(filePath);
            boolean modified = false;

            for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                for (MethodDeclaration method : clazz.getMethods()) {
                    if (optimizeMethod(method)) {
                        modified = true;
                    }
                }
            }

            if (modified) {
                // Remove unused net.datastream imports if no datastream symbols remain in the file text
                String fileContent = cu.toString();
                if (!fileContent.contains("MP0") && !fileContent.contains("MP1") && !fileContent.contains("MP2") &&
                    !fileContent.contains("MP3") && !fileContent.contains("MP4") && !fileContent.contains("MP5") &&
                    !fileContent.contains("MP6") && !fileContent.contains("MP7") && !fileContent.contains("MP8") &&
                    !fileContent.contains("MP9")) {
                    cu.getImports().removeIf(imp -> imp.getNameAsString().startsWith("net.datastream"));
                }
                Files.write(filePath, cu.toString().getBytes("UTF-8"));
                System.out.println("  [OPTIMIZED] " + filePath.getFileName());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error processing file " + filePath + ": " + e.getMessage());
        }
        return false;
    }

    private static boolean optimizeMethod(MethodDeclaration method) {
        if (!method.getBody().isPresent()) return false;
        BlockStmt body = method.getBody().get();
        List<Statement> stmts = body.getStatements();

        if (stmts.size() > 1 && stmts.get(0).isIfStmt()) {
            IfStmt ifStmt = stmts.get(0).asIfStmt();
            String ifCondition = ifStmt.getCondition().toString();
            
            // Check if this is a JPA check injected by JpaMigrationRefactorer or existing code
            if (ifCondition.toLowerCase().contains("repository != null")) {
                Statement thenStmt = ifStmt.getThenStmt();
                BlockStmt targetBlock = null;
                if (thenStmt.isBlockStmt()) {
                    targetBlock = thenStmt.asBlockStmt();
                    if (targetBlock.getStatements().size() == 1 && targetBlock.getStatement(0).isTryStmt()) {
                        targetBlock = targetBlock.getStatement(0).asTryStmt().getTryBlock();
                    }
                } else if (thenStmt.isTryStmt()) {
                    targetBlock = thenStmt.asTryStmt().getTryBlock();
                }

                if (targetBlock != null) {
                    boolean hasReturn = targetBlock.getStatements().stream().anyMatch(Statement::isReturnStmt);
                    if (hasReturn) {
                        body.getStatements().clear();
                        for (Statement s : targetBlock.getStatements()) {
                            body.addStatement(s);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
