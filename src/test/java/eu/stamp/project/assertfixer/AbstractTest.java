package eu.stamp.project.assertfixer;

import eu.stamp.project.assertfixer.util.Util;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseDescription;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/18
 */
public class AbstractTest {

    protected static String dependenciesToRunJUnit;
    protected static Configuration configuration;

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

        configuration = new Configuration();
        configuration.setClasspath(dependenciesToRunJUnit);
        configuration.setFullQualifiedFailingTestClass("aPackage.ClassResourcesTest");
        configuration.setFailingTestMethods(Arrays.asList("testAssertionErrorBoolean:testAssertionErrorPrimitive"));
        configuration.setPathToTestFolder(Arrays.asList("src/test/resources"));
        configuration.setVerbose(true);
        configuration.setOutput("target/assert-fixer");
        configuration.setGenTryCatch(true);

        spoon = new Launcher();
        for (String path : configuration.getPathToTestFolder()) {
            spoon.addInputResource(path);
        }
        spoon.addInputResource("src/main/java/eu/stamp/project/assertfixer/asserts/log/Logger.java"); // adding the logger to the spoon model to compile and run it to fix assertions
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.getEnvironment().setCommentEnabled(true);
        spoon.setSourceOutputDirectory(configuration.getSourceOutputDirectory());
        spoon.setBinaryOutputDirectory(configuration.getBinaryOutputDirectory());
        spoon.run();
        compiler = spoon.createCompiler();
        compiler.setBinaryOutputDirectory(new File(configuration.getBinaryOutputDirectory()));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileUtils.forceDelete(new File("target/assert-fixer"));
    }

    protected static String getClasspath() {
        return "target/assert-fixer/spooned-classes" + Util.PATH_SEPARATOR + dependenciesToRunJUnit;
    }

}