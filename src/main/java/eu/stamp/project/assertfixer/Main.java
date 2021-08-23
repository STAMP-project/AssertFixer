package eu.stamp.project.assertfixer;

import eu.stamp.project.assertfixer.asserts.AssertFixer;
import eu.stamp.project.assertfixer.test.TestRunner;
import eu.stamp.project.assertfixer.util.Util;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Main {

    static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Configuration configuration;

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
        final Boolean result = this.runWithResults().stream()
                .map(AssertFixerResult::isSuccess)
                .reduce(Boolean.TRUE, Boolean::logicalAnd);
        if (result) {
            return 1;
        } else {
            return 0;
        }
    }

    public List<AssertFixerResult> runWithResults() {
        List<AssertFixerResult> allResults = new ArrayList<>();
        Launcher launcher = this.getSpoonAPIForProject();

        if (this.configuration.getMultipleTestCases() != null) {
            Map<String, List<String>> multipleTestCases = this.configuration.getMultipleTestCases();
            for (String testClass : multipleTestCases.keySet()) {
                for (String testMethod : multipleTestCases.get(testClass)) {
                    allResults.add(this.fixGivenTest(launcher, testClass, testMethod));
                }
            }
        } else {
            for (String testMethod : this.configuration.getFailingTestMethods()) {
                allResults.add(this.fixGivenTest(launcher, this.configuration.getFullQualifiedFailingTestClass(), testMethod));
            }
        }

        return allResults;
    }

    private Launcher getSpoonAPIForProject() {
        EntryPoint.verbose = this.configuration.isVerbose();

        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setSourceClasspath(this.configuration.getClasspath().split(Util.PATH_SEPARATOR));
        if (this.configuration.getPathToSourceFolder() != null) {
            for (String path : this.configuration.getPathToSourceFolder()) {
                launcher.addInputResource(path);
            }

        }

        if (this.configuration.getPathToTestFolder() == null) {
            throw new RuntimeException("Test folder must be set for AssertFixer to be executed.");
        }
        for (String path : this.configuration.getPathToTestFolder()) {
            launcher.addInputResource(path);
        }

        launcher.getEnvironment().setShouldCompile(true);
        launcher.run();

        return launcher;
    }

    private AssertFixerResult fixGivenTest(Launcher launcher, String failingClass, String failingTestMethod) {
        CtClass testClass = launcher.getFactory().Class().get(failingClass);

        Failure failure = TestRunner.runTest(this.configuration, launcher, failingClass, failingTestMethod)
                .getFailingTests().stream().findFirst().orElseThrow(RuntimeException::new);
        LOGGER.info("Fixing: {}", failure.messageOfFailure);
        return AssertFixer.fixAssert(
                    configuration,
                    launcher,
                    testClass,
                    failingTestMethod,
                    failure,
                    this.configuration.getClasspath()
            );
    }
}
