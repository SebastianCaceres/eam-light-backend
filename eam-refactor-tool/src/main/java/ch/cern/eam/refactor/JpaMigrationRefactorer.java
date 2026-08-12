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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to automate the migration of ServiceImpl classes from SOAP to JPA.
 * This skeleton sets up JavaParser with Symbol Solving to correctly identify
 * InforWebServicesPT method calls and replace them with JPA repository calls.
 */
public class JpaMigrationRefactorer {

    private static final String SRC_DIR = "eam-wshub-core/src/main/java";

    public static void main(String[] args) throws IOException {
        // 1. Set up Type Solver to resolve types
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        // 2. Find all ServiceImpl files
        Path coreSrcPath = Paths.get(SRC_DIR).toAbsolutePath().normalize();
        System.out.println("Searching in path: " + coreSrcPath);

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

    private static void processFile(File file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);

            cu.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(classDecl -> {
                Map<String, String> repoToEntityMap = new HashMap<>();
                classDecl.getFields().forEach(f -> {
                    String type = f.getVariable(0).getTypeAsString();
                    String name = f.getVariable(0).getNameAsString();
                    if (type.endsWith("Repository")) {
                        String entity = type.replace("Repository", "");
                        if (entity.equals("Equipment")) entity = "Equipment";
                        else if (entity.equals("EAMUser")) entity = "EAMUser";
                        repoToEntityMap.put(name, entity);
                    }
                });

                if (!repoToEntityMap.isEmpty()) {
                    String primaryRepo = repoToEntityMap.keySet().iterator().next();
                    String primaryEntity = repoToEntityMap.get(primaryRepo);
                    System.out.println("\n[REFACTORING] " + classDecl.getNameAsString() + " using repository: " + primaryRepo + " (" + primaryEntity + ")");
                    boolean[] modified = {false};

                    classDecl.getMethods().forEach(md -> {
                        String mName = md.getNameAsString();
                        String bodyStr = md.getBody().map(Object::toString).orElse("");

                        // Skip batch methods, methods with List parameters, or methods already containing repository check
                        if (mName.endsWith("Batch") || bodyStr.contains(primaryRepo) || (md.getParameters().size() >= 2 && md.getParameter(1).getTypeAsString().contains("List"))) {
                            return;
                        }

                        // Parameter checks
                        String paramType = md.getParameters().size() >= 2 ? md.getParameter(1).getTypeAsString() : "";
                        String getter = "getCode()";
                        if (paramType.equals("WorkOrder")) {
                            getter = "getNumber()";
                        } else if (paramType.equals("Comment")) {
                            getter = "getPk()";
                        } else if (paramType.equals("EAMUser")) {
                            getter = "getUserCode()";
                        } else if (paramType.equals("InforCaseTask")) {
                            getter = "getTaskCode()";
                        } else if (paramType.equals("EquipmentPMSchedule")) {
                            getter = "getPmCode()";
                        }

                        // Determine CRUD pattern - only when paramType matches expected entity type
                        if (mName.startsWith("read") && md.getParameters().size() >= 2 && md.getTypeAsString().equals(primaryEntity)) {
                            String codeParam = md.getParameter(1).getNameAsString();
                            String stmtStr = String.format(
                                    "if (%s != null && %s != null) {\n" +
                                    "    java.util.Optional opt = %s.findById(%s);\n" +
                                    "    if (opt.isPresent()) return (%s) opt.get();\n" +
                                    "}", primaryRepo, codeParam, primaryRepo, codeParam, primaryEntity);
                            md.getBody().ifPresent(body -> {
                                body.addStatement(0, StaticJavaParser.parseStatement(stmtStr));
                                modified[0] = true;
                                System.out.println("  + Added JPA fallback to " + mName);
                            });
                        } else if (mName.startsWith("create") && md.getParameters().size() >= 2 && paramType.equals(primaryEntity) && md.getTypeAsString().equals("String")) {
                            String entityParam = md.getParameter(1).getNameAsString();
                            String stmtStr = String.format(
                                    "if (%s != null) {\n" +
                                    "    try {\n" +
                                    "        %s saved = %s.save(%s);\n" +
                                    "        return saved.%s;\n" +
                                    "    } catch (Exception e) {\n" +
                                    "        // Fallback to SOAP\n" +
                                    "    }\n" +
                                    "}", primaryRepo, primaryEntity, primaryRepo, entityParam, getter);
                            md.getBody().ifPresent(body -> {
                                body.addStatement(0, StaticJavaParser.parseStatement(stmtStr));
                                modified[0] = true;
                                System.out.println("  + Added JPA fallback to " + mName);
                            });
                        } else if (mName.startsWith("update") && md.getParameters().size() >= 2 && paramType.equals(primaryEntity) && md.getTypeAsString().equals("String")) {
                            String entityParam = md.getParameter(1).getNameAsString();
                            String stmtStr = String.format(
                                    "if (%s != null) {\n" +
                                    "    try {\n" +
                                    "        %s saved = %s.save(%s);\n" +
                                    "        return saved.%s;\n" +
                                    "    } catch (Exception e) {\n" +
                                    "        // Fallback to SOAP\n" +
                                    "    }\n" +
                                    "}", primaryRepo, primaryEntity, primaryRepo, entityParam, getter);
                            md.getBody().ifPresent(body -> {
                                body.addStatement(0, StaticJavaParser.parseStatement(stmtStr));
                                modified[0] = true;
                                System.out.println("  + Added JPA fallback to " + mName);
                            });
                        } else if (mName.startsWith("delete") && md.getParameters().size() >= 2 && paramType.equals("String") && md.getTypeAsString().equals("String")) {
                            String codeParam = md.getParameter(1).getNameAsString();
                            String stmtStr = String.format(
                                    "if (%s != null && %s != null) {\n" +
                                    "    try {\n" +
                                    "        %s.deleteById(%s);\n" +
                                    "        return %s;\n" +
                                    "    } catch (Exception e) {\n" +
                                    "        // Fallback to SOAP\n" +
                                    "    }\n" +
                                    "}", primaryRepo, codeParam, primaryRepo, codeParam, codeParam);
                            md.getBody().ifPresent(body -> {
                                body.addStatement(0, StaticJavaParser.parseStatement(stmtStr));
                                modified[0] = true;
                                System.out.println("  + Added JPA fallback to " + mName);
                            });
                        }
                    });

                    if (modified[0]) {
                        try {
                            Files.write(file.toPath(), cu.toString().getBytes());
                            System.out.println("  => Saved changes to " + file.getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error parsing " + file.getName() + ": " + e.getMessage());
        }
    }
}
