package tck.jakarta.platform.rewrite;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

import jakartatck.jar2shrinkwrap.Jar2ShrinkWrap;
import jakartatck.jar2shrinkwrap.JarProcessor;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import tck.jakarta.platform.rewrite.mapping.ClassNameRemappingImpl;
import tck.jakarta.platform.rewrite.mapping.EE11_2_EE10;

/**
 * AddArquillianDeployMethodRecipe
 *
 * @author Scott Marlow
 */
public class AddArquillianDeployMethodRecipe extends Recipe implements Serializable {

    static final long serialVersionUID = 427023419L;
    private static String fullyQualifiedClassName = AddArquillianDeployMethodRecipe.class.getCanonicalName();

    static {
        System.out.println("Preparing to use " + fullyQualifiedClassName);
    }

    @Override
    public String getDisplayName() {
        return "JavaTest to Arquillian/Shrinkwrap/Junit5";
    }

    @Override
    public String getDescription() {
        return "Main entry point for the JavaTest to Arquillian/Shrinkwrap based TCK tests.";
    }

    @Override
    public String toString() {
        return fullyQualifiedClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        AddArquillianDeployMethodRecipe that = (AddArquillianDeployMethodRecipe) o;
        return Objects.equals(fullyQualifiedClassName, that.fullyQualifiedClassName);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new testClassVisitor();
    }

    public class testClassVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
            // Don't make changes to classes that don't match the fully qualified name
            //if (classDecl.getType() == null || !classDecl.getType().getFullyQualifiedName().equals(fullyQualifiedClassName)) {
            //    System.out.println("classDecl.getType() (" +classDecl.getType() + ") is not equal to " + fullyQualifiedClassName);
            //    return classDecl;
            // }

            // Check if the class already has a method named "deployment".
            boolean deploymentMethodExists = classDecl.getBody().getStatements().stream()
                    .filter(statement -> statement instanceof J.MethodDeclaration)
                    .map(J.MethodDeclaration.class::cast)
                    .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals("deployment"));

            // If the class already has a `deployment()` method, don't make any changes to it.
            if (deploymentMethodExists) {
                return classDecl;
            }

            boolean isEETest = classDecl.getSimpleName().contains("Client"); // this will match too much but still try

            List<J.Modifier> modifiers = classDecl.getModifiers();
            boolean isAbstract = modifiers.stream().anyMatch(modifier -> modifier.getType() == J.Modifier.Type.Abstract);

            // return if this is not an EE test
            if(isAbstract || !isEETest) {
                return classDecl;
            }

            String pkg = classDecl.getType().getPackageName();
            String ee10pkg = EE11_2_EE10.getEE11mapping(pkg);

            // Generate the deployment() method
            JarProcessor jarProcessor = Jar2ShrinkWrap.fromPackage(ee10pkg, ClassNameRemappingImpl.getInstance());
            StringWriter methodCodeWriter = new StringWriter();
            jarProcessor.saveOutput(methodCodeWriter, false);
            String methodCode = methodCodeWriter.toString();
            if (methodCode.length() == 0) {
                // we shouldn't hit this case but still check for it
                return classDecl;
            }


            final JavaTemplate deploymentTemplate =
                    JavaTemplate.builder(methodCode).imports()
                            .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                            .build();
            maybeAddImport("org.jboss.arquillian.container.test.api.Deployment");
            maybeAddImport("org.jboss.shrinkwrap.api.Archive");
            maybeAddImport("org.jboss.shrinkwrap.api.ShrinkWrap");
            maybeAddImport("org.jboss.shrinkwrap.api.spec.EnterpriseArchive");
            maybeAddImport("org.jboss.shrinkwrap.api.spec.WebArchive");
            maybeAddImport("org.jboss.shrinkwrap.api.spec.JavaArchive");

            // Interpolate the fullyQualifiedClassName into the template and use the resulting LST to update the class body
            try {

                //classDecl = classDecl.withBody(deploymentTemplate.apply(new Cursor(getCursor(), classDecl.getBody()),
                //        classDecl.getBody().getCoordinates().lastStatement(),
                //        fullyQualifiedClassName));
                classDecl.withBody( deploymentTemplate.apply(new Cursor(getCursor(), classDecl.getBody()),
                                        classDecl.getBody().getCoordinates().firstStatement()));
            } catch (RuntimeException e) {
                System.out.println("error " + e.getMessage() + "occurred for (EE11) " + pkg + " (EE10) " + ee10pkg +
                        " classDecl.getType() = " + classDecl.getType() + "methodCode " + methodCode);
                e.printStackTrace();
            }
            return classDecl;
        }
    }
}
