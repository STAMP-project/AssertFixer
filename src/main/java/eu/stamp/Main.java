package eu.stamp;

import eu.stamp.asserts.AssertFixer;
import eu.stamp.runner.test.Failure;
import eu.stamp.runner.test.TestRunner;
import eu.stamp.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

import java.io.File;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Main {

    static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static Configuration configuration;

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String [] args) {
        Main.configuration = Configuration.get(args);
        EntryPoint.verbose = Main.configuration.verbose;

        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setSourceClasspath(configuration.classpath.split(Util.PATH_SEPARATOR));
        launcher.addInputResource(configuration.pathToSourceFolder);
        launcher.addInputResource(configuration.pathToTestFolder);
        launcher.getEnvironment().setShouldCompile(true);
        launcher.run();

        final Boolean result = Main.configuration.failingTestMethods.stream()
                .map(failingTestMethod -> fixGivenTest(launcher, failingTestMethod))
                .reduce(Boolean.TRUE, Boolean::logicalAnd);
        if (result) {
            return 1;
        } else {
            return 0;
        }
    }

    private static boolean fixGivenTest(Launcher launcher, String failingTestMethod) {
        Failure failure = eu.stamp.test.TestRunner.runTest(launcher, failingTestMethod).getFailingTests().get(0);
        LOGGER.info("Fixing: {}", failure.messageOfFailure);
        try {
            AssertFixer.fixAssert(
                    launcher,
                    configuration.fullQualifiedFailingTestClass,
                    failingTestMethod,
                    failure,
                    configuration.classpath
            );
            final SpoonModelBuilder compiler = launcher.createCompiler();
            compiler.setBinaryOutputDirectory(new File(Main.configuration.output));
            return EntryPoint.runTests(
                    Main.configuration.getBinaryOutputDirectory() +
                            Util.PATH_SEPARATOR + configuration.classpath,
                    configuration.fullQualifiedFailingTestClass,
                    failingTestMethod
            ).getFailingTests().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
