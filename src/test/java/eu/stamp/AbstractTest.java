package eu.stamp;

import eu.stamp.util.Util;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseDescription;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

import java.io.File;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/18
 */
public class AbstractTest {

    protected static String dependenciesToRunJUnit;

    static {
        try {
            dependenciesToRunJUnit = new File(
                    Test.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .toString() + ":" +
                    new File(
                            BaseDescription.class.getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI())
                            .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static Launcher spoon;
    protected static SpoonModelBuilder compiler;

    @BeforeClass
    public static void setUp() throws Exception {
        Main.configuration = Configuration.get(new String[]{
                "--classpath", dependenciesToRunJUnit,
                "--test-class", "aPackage.ClassResourcesTest",
                "--test-method", "testAssertionErrorBoolean:testAssertionErrorPrimitive",
                "--source-path", "src/test/resources/ClassResourcesTest.java",
                "--test-path", "src/test/resources/ClassResourcesTest.java",
                "--verbose",
                "--output", "target/assert-fixer"
        });
        spoon = new Launcher();
        spoon.addInputResource("src/test/resources/ClassResourcesTest.java");
        spoon.addInputResource("src/main/java/eu/stamp/asserts/log/Logger.java"); // adding the logger to the spoon model to compile and run it to fix assertions
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.setSourceOutputDirectory("target/assert-fixer/spooned");
        spoon.setBinaryOutputDirectory("target/assert-fixer/spooned-classes");
        spoon.run();
        compiler = spoon.createCompiler();
        compiler.setBinaryOutputDirectory(new File("target/assert-fixer/spooned-classes"));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileUtils.forceDelete(new File("target/assert-fixer"));
    }

    protected static String getClasspath() {
        return "target/assert-fixer/spooned-classes" + Util.PATH_SEPARATOR + dependenciesToRunJUnit;
    }

}