package eu.stamp.project.assertfixer;

import eu.stamp.project.assertfixer.asserts.log.Logger;
import eu.stamp.project.assertfixer.util.Util;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseDescription;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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

    protected String dependencies;
    protected Configuration configuration;

	public final static String pathdep = "target/dependency-binary";


	public static String getJarPath(Class c) throws URISyntaxException {
		return new File(
				c.getProtectionDomain()
						.getCodeSource()
						.getLocation()
						.toURI())
				.getAbsolutePath();
	}


	protected static Launcher spoon;

    @Before
    public void setUp() throws Exception {
		dependencies = getJarPath(Test.class)
				+ Util.PATH_SEPARATOR + getJarPath(BaseDescription.class)
				+ Util.PATH_SEPARATOR + getJarPath(Logger.class)
				+ Util.PATH_SEPARATOR + pathdep
		;
		Launcher preparation = new Launcher();
		preparation.addInputResource("src/test/resources/NotExist.java");
		preparation.getEnvironment().setComplianceLevel(7);
		preparation.getEnvironment().setAutoImports(true);
		preparation.getEnvironment().setShouldCompile(true);
		preparation.getEnvironment().setCommentEnabled(true);
		preparation.setBinaryOutputDirectory(pathdep);
		preparation.run();


        configuration = new Configuration();
        configuration.setClasspath(dependencies);
        configuration.setFullQualifiedFailingTestClass("aPackage.ClassResourcesTest");
        configuration.setFailingTestMethods(Arrays.asList("testAssertionErrorBoolean:testAssertionErrorPrimitive"));
        configuration.setPathToTestFolder(Arrays.asList("src/test/resources"));
        configuration.setVerbose(true);
        configuration.setOutput("target/assert-fixer");
        configuration.setGenTryCatch(true);

        spoon = new Launcher();
        spoon.addInputResource("src/test/resources/ClassResourcesTest.java");
		spoon.getModelBuilder().setSourceClasspath(configuration.getClasspath().split(Util.PATH_SEPARATOR));
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.getEnvironment().setCommentEnabled(true);
        spoon.setSourceOutputDirectory(configuration.getSourceOutputDirectory());
        spoon.setBinaryOutputDirectory(configuration.getBinaryOutputDirectory());
        spoon.run();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.forceDelete(new File("target/assert-fixer"));
    }

    protected String getClasspath() {
        return  dependencies + Util.PATH_SEPARATOR + configuration.getBinaryOutputDirectory();
    }

}