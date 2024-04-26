package tck.jakarta.platform.rewrite;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * AddArquillianDeployMethodRecipe
 *
 * @author Scott Marlow
 */
public class AddArquillianDeployMethodRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "JavaTest to Arquillian/Shrinkwrap/Junit5";
    }

    @Override
    public String getDescription() {
        return "Main entry point for the JavaTest to Arquillian/Shrinkwrap based TCK tests.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>("org.openrewrite.Recipe", true), new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                return new AddArquillianDeployMethod<>().visitClassDeclaration(classDecl, executionContext);
            }
        });
    }

}
