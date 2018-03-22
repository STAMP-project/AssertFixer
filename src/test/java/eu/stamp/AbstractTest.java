package eu.stamp;

import eu.stamp.util.Util;
import org.hamcrest.BaseDescription;
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
    protected static Launcher spoon;
    protected static SpoonModelBuilder compiler;

    @BeforeClass
    public static void setUp() throws Exception {
        spoon = new Launcher();
        spoon.addInputResource("src/test/resources/ClassResourcesTest.java");
        spoon.addInputResource("src/main/java/eu/stamp/asserts/log/Logger.java"); // adding the logger to the spoon model to compile and run it to fix assertions
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.setSourceOutputDirectory(Main.configuration.getSourceOutputDirectory());
        spoon.setBinaryOutputDirectory(Main.configuration.getBinaryOutputDirectory());
        spoon.run();
        compiler = spoon.createCompiler();
        compiler.setBinaryOutputDirectory(new File(Main.configuration.getBinaryOutputDirectory()));
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

    protected static String getClasspath() {
        return Main.configuration.getBinaryOutputDirectory() + Util.PATH_SEPARATOR + dependenciesToRunJUnit;
    }

}