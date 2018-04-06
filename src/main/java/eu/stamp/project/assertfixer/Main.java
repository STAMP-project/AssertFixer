package eu.stamp.project.assertfixer;

import eu.stamp.project.assertfixer.asserts.AssertFixer;
import eu.stamp.project.assertfixer.test.TestRunner;
import eu.stamp.project.assertfixer.util.Util;
import eu.stamp.project.testrunner.EntryPoint;
import eu.stamp.project.testrunner.runner.test.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.SpoonModelBuilder;

import java.io.File;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Main {

    static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    Configuration configuration;

    public Main(Configuration configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args) {
        System.exit(Main.run(args));
    }

    public static int run(String[] args) {
        Configuration configuration = JSAPConfiguration.get(args);
        Main main = new Main(configuration);
        return main.run();
    }

    public int run() {
        Launcher launcher = this.getSpoonAPIForProject();

        final Boolean result = this.configuration.getFailingTestMethods().stream()
                .map(failingTestMethod -> this.fixGivenTest(launcher, failingTestMethod))
                .reduce(Boolean.TRUE, Boolean::logicalAnd);
        if (result) {
            return 1;
        } else {
            return 0;
        }
    }

    private Launcher getSpoonAPIForProject() {
        EntryPoint.verbose = this.configuration.isVerbose();

        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setSourceClasspath(this.configuration.getClasspath().split(Util.PATH_SEPARATOR));
        launcher.addInputResource(this.configuration.getPathToSourceFolder());
        launcher.addInputResource(this.configuration.getPathToTestFolder());
        launcher.getEnvironment().setShouldCompile(true);
        launcher.run();

        return launcher;
    }

    private boolean fixGivenTest(Launcher launcher, String failingTestMethod) {
        Failure failure = TestRunner.runTest(this.configuration, launcher, failingTestMethod).getFailingTests().get(0);
        LOGGER.info("Fixing: {}", failure.messageOfFailure);
        try {
            AssertFixer.fixAssert(
                    configuration,
                    launcher,
                    this.configuration.getFullQualifiedFailingTestClass(),
                    failingTestMethod,
                    failure,
                    this.configuration.getClasspath()
            );
            return EntryPoint.runTests(
                    this.configuration.getBinaryOutputDirectory() +
                            Util.PATH_SEPARATOR + configuration.getClasspath(),
                    configuration.getFullQualifiedFailingTestClass(),
                    failingTestMethod
            ).getFailingTests().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
