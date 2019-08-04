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
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/18
 */
public class AbstractTest {

    protected static String dependenciesToRunJUnit;
    protected static Configuration configuration;

	public final static String junitJarPath = getJunitPath();
	public final static String pathdep = "target/dependency-binary";

    static {
        try {
			dependenciesToRunJUnit = junitJarPath + ":" +
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

	private static String getJunitPath() {
		try {
			return new File(
					Test.class.getProtectionDomain()
							.getCodeSource()
							.getLocation()
							.toURI()).getAbsolutePath();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	protected static Launcher spoon;
    protected static SpoonModelBuilder compiler;

    @BeforeClass
    public static void setUp() throws Exception {
		Launcher preparation = new Launcher();
		preparation.addInputResource("src/test/resources/NotExist.java");
		preparation.getEnvironment().setComplianceLevel(7);
		preparation.getEnvironment().setAutoImports(true);
		preparation.getEnvironment().setShouldCompile(true);
		preparation.getEnvironment().setCommentEnabled(true);
		preparation.setBinaryOutputDirectory(pathdep);
		preparation.run();


        configuration = new Configuration();
        configuration.setClasspath(dependenciesToRunJUnit+":"+pathdep);
        configuration.setFullQualifiedFailingTestClass("aPackage.ClassResourcesTest");
        configuration.setFailingTestMethods(Arrays.asList("testAssertionErrorBoolean:testAssertionErrorPrimitive"));
        configuration.setPathToTestFolder(Arrays.asList("src/test/resources"));
        configuration.setVerbose(true);
        configuration.setOutput("target/assert-fixer");
        configuration.setGenTryCatch(true);

        spoon = new Launcher();
        spoon.addInputResource("src/test/resources/ClassResourcesTest.java");
        spoon.addInputResource("src/main/java/eu/stamp/project/assertfixer/asserts/log/Logger.java"); // adding the logger to the spoon model to compile and run it to fix assertions
		spoon.getModelBuilder().setSourceClasspath(junitJarPath, pathdep);
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
        return  dependenciesToRunJUnit + Util.PATH_SEPARATOR + pathdep + Util.PATH_SEPARATOR + configuration.getBinaryOutputDirectory();
    }

}