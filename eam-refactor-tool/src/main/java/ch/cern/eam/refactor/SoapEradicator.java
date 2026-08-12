package ch.cern.eam.refactor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.Statement;

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
                .filter(p -> p.toString().endsWith("Impl.java"))
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
                
                // 1. Process methods that use SOAP types
                for (MethodDeclaration m : clazz.getMethods()) {
                    String methodText = m.toString();
                    boolean usesSoap = methodText.contains("MP0") || methodText.contains("MP1") || methodText.contains("MP2") ||
                                       methodText.contains("MP3") || methodText.contains("MP4") || methodText.contains("MP5") ||
                                       methodText.contains("MP6") || methodText.contains("MP7") || methodText.contains("MP8") ||
                                       methodText.contains("MP9") || methodText.contains("_Type") || methodText.contains("EquipmentCategory") ||
                                       methodText.contains("AssetEquipment") || methodText.contains("SystemEquipment") ||
                                       methodText.contains("PositionEquipment") || methodText.contains("AssetParentHierarchy") ||
                                       methodText.contains("SystemParentHierarchy") || methodText.contains("NonconformityObservation") ||
                                       methodText.contains("CustomerRentalAdjustment") || methodText.contains("EquipmentConfiguration") ||
                                       methodText.contains("PickList") || methodText.contains("ExtMenus") || methodText.contains("NonPOReceipt") ||
                                       methodText.contains("USERDEFINEDSCREENFIELDVALUEPAIR") || methodText.contains("performInforOperation") ||
                                       methodText.contains("CaseManagement") || methodText.contains("EntitySafety") ||
                                       methodText.contains("WorkSafety") || methodText.contains("ESIGNATURE");

                    if (usesSoap) {
                        if (m.isPublic()) {
                            // Stub public interface methods instead of removing them
                            if (m.getBody().isPresent()) {
                                m.getBody().get().getStatements().clear();
                                String retType = m.getTypeAsString();
                                if (retType.equals("void")) {
                                    // no return statement needed
                                } else if (retType.equals("boolean")) {
                                    m.getBody().get().addStatement(StaticJavaParser.parseStatement("return false;"));
                                } else if (retType.equals("int") || retType.equals("long") || retType.equals("double")) {
                                    m.getBody().get().addStatement(StaticJavaParser.parseStatement("return 0;"));
                                } else {
                                    m.getBody().get().addStatement(StaticJavaParser.parseStatement("return null;"));
                                }
                                modified = true;
                            }
                        } else {
                            // Remove private/protected methods
                            m.remove();
                            modified = true;
                        }
                    }
                }

                // 2. Remove InforWebServicesPT field
                List<FieldDeclaration> fieldsToRemove = clazz.getFields().stream()
                        .filter(f -> f.getVariable(0).getTypeAsString().equals("InforWebServicesPT"))
                        .collect(Collectors.toList());
                for (FieldDeclaration field : fieldsToRemove) {
                    field.remove();
                    modified = true;
                }

                // 3. Remove InforWebServicesPT from constructors and its assignment
                for (ConstructorDeclaration constructor : clazz.getConstructors()) {
                    List<Parameter> paramsToRemove = constructor.getParameters().stream()
                            .filter(p -> p.getTypeAsString().equals("InforWebServicesPT"))
                            .collect(Collectors.toList());
                    for (Parameter param : paramsToRemove) {
                        String paramName = param.getNameAsString();
                        constructor.getParameters().remove(param);
                        if (constructor.getBody().getStatements() != null) {
                            List<Statement> statementsToRemove = constructor.getBody().getStatements().stream()
                                    .filter(s -> s.toString().contains("inforws") || s.toString().contains("inforWebServicesToolkitClient"))
                                    .collect(Collectors.toList());
                            for (Statement stmt : statementsToRemove) {
                                stmt.remove();
                            }
                        }
                        modified = true;
                    }
                }
            }

            // 3. Remove all net.datastream imports unconditionally from ServiceImpl files
            boolean importsRemoved = cu.getImports().removeIf(imp -> imp.getNameAsString().startsWith("net.datastream"));
            if (importsRemoved) {
                modified = true;
            }

            if (modified) {
                Files.write(filePath, cu.toString().getBytes("UTF-8"));
                System.out.println("  [OPTIMIZED] " + filePath.getFileName());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error processing file " + filePath + ": " + e.getMessage());
        }
        return false;
    }
}
