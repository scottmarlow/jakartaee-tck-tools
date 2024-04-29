package tck.jakarta.platform.rewrite.mapping;

/**
 * EE11 to EE 10 mappings
 *
 * @author Scott Marlow
 */
public class EE11_2_EE10 {
    public static String getEE11mapping(String classname) {
        if (classname.startsWith("ee.jakarta.tck.persistence.core")) {
            return classname.replace("ee.jakarta.tck.persistence.core","com.sun.ts.tests.jpa.core");
        }
        throw new RuntimeException("EE11_2_EE10 needs to be updated to handle converting class " + classname + " to its equivalent EE 10 Platform TCK test name.");
    }
}
