package tck.jakarta.platform.rewrite.mapping;

import jakartatck.jar2shrinkwrap.ClassNameRemapping;

/**
 * ClassNameRemappingImpl will map EE 10 TCK test class names to their equivalent EE 11 names
 *
 * @author Scott Marlow
 */
public class ClassNameRemappingImpl implements ClassNameRemapping {

    private static final ClassNameRemappingImpl instance = new ClassNameRemappingImpl();

    public static ClassNameRemappingImpl getInstance() {
        return instance;
    }

    public String getName(String className) {
        return className.replace("com.sun.ts.tests.jpa.common.schema30","ee.jakarta.tck.persistence.common.schema30").
                         replace("com.sun.ts.tests.jpa","ee.jakarta.tck.persistence");
    }

    @Override
    public boolean shouldBeIgnored(String className) {
        // do not put TCK vehicle classes in the (ShrinkWrap) test deployment archives
        if(className != null && className.startsWith("com.sun.ts.tests.common.vehicle")) {
            return true;
        }
        return false;
    }
}
