package fr.inria.spirals.run;

import fr.inria.spirals.asserts.log.Logger;
import fr.inria.spirals.json.JSONTest;
import fr.inria.spirals.test.TestRunner;
import fr.inria.spirals.util.Counter;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.TestTimedOutException;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static fr.inria.spirals.asserts.AssertFixer.fixAssert;
import static fr.inria.spirals.util.Util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class Run {

    public enum RepairCode {REPAIRED, NO_FAILURE, TIMEOUT}

    public static List<JSONTest> runAllTestCaseName(String project, String bugId, String seed, String[] testsCaseName, String fullQualifiedName) throws Throwable {
        System.out.println(project + "#" + bugId + "::" + seed);
        List<JSONTest> testsResults = new ArrayList<>();
        Launcher spoon = buildSpoonModel(project, bugId, seed);
        String cp = getBaseClassPath(project, bugId);
        cp += PATH_SEPARATOR + spoon.getEnvironment().getBinaryOutputDirectory();
        for (String indexTest : testsCaseName) {
            String testCaseName = indexToRealTestCaseName(spoon, indexTest, fullQualifiedName);
            RepairCode code = run(spoon, project, bugId, seed, testCaseName, fullQualifiedName);
            final SpoonModelBuilder compiler = spoon.createCompiler();
            final boolean compile = compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
            if (!compile) {
                throw new RuntimeException();
            }
            final List<Failure> failures = TestRunner.runTest(fullQualifiedName, testCaseName, cp.split(":"));
            testsResults.add(new JSONTest(testCaseName, failures.isEmpty(), failures.toString(),
                    code == RepairCode.NO_FAILURE, code == RepairCode.TIMEOUT));
            if (failures.isEmpty()) {
                Counter.incNumberOfFixedTests();
            } else {
                Counter.incNumberOfNotFixedTests();
            }
            Counter.updateNumberOfFailingAssertionInTests();
        }
        final CtClass<Object> loggerCtClass = spoon.getFactory().Class().get(Logger.class);
        loggerCtClass.delete();
        loggerCtClass.updateAllParentsBelow();
        spoon.prettyprint();
        return testsResults;
    }

    public static RepairCode run(Launcher spoon, String project, String bugId, String seed, String testCaseName, String fullQualifiedName) throws Throwable {
        String cp = getBaseClassPath(project, bugId);
        cp += PATH_SEPARATOR + spoon.getEnvironment().getBinaryOutputDirectory();
        List<Failure> failures = TestRunner.runTest(
                fullQualifiedName,
                testCaseName,
                cp.split(":"));// should fail bug exposing test

        if (failures.isEmpty()) {
            System.err.println("No failure has been found for: ");
            System.err.println(project + "#" + bugId + "::" + seed + "<" + testCaseName + ">");
            return RepairCode.NO_FAILURE;
        }

        if (failures.get(0).getException() instanceof TestTimedOutException) {
            System.err.println("Timeout Exception");
            System.err.println(project + "#" + bugId + "::" + seed + "<" + testCaseName + ">");
            return RepairCode.TIMEOUT;
        }

        fixAssert(spoon,
                fullQualifiedName,
                testCaseName,
                failures.get(0),
                cp);

        failures = TestRunner.runTest(
                fullQualifiedName,
                testCaseName,
                cp.split(":"));

        if (gotAssertionError.test(failures)) {
            return run(spoon, project, bugId, seed, testCaseName, fullQualifiedName);
        }

        return RepairCode.REPAIRED; // TODO add other cases such as flaky, or not repaired
    }

    private static Predicate<List<Failure>> gotAssertionError = (failures ->
            failures.stream().anyMatch(failure -> failure.getException() instanceof AssertionError));


    private static Launcher buildSpoonModel(String project, String bugId, String seed) {
        Launcher spoon = new Launcher();
        spoon.addInputResource(PATH_TO_TEST4REPAIR_RESULTS + project + FILE_SEPARATOR + bugId + FILE_SEPARATOR + seed);
        spoon.addInputResource("src/main/java/fr/inria/spirals/asserts/log/Logger.java"); // adding the logger to the spoon model to compile and run it to fix assertions
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setCommentEnabled(true);
        spoon.getEnvironment().setShouldCompile(true);
        String cp = getBaseClassPath(project, bugId);
        spoon.getEnvironment().setSourceClasspath((cp).split(PATH_SEPARATOR));
        spoon.run();
        spoon.setSourceOutputDirectory("output" + FILE_SEPARATOR + project + FILE_SEPARATOR + bugId + FILE_SEPARATOR + seed);
        return spoon;
    }

    private static String indexToRealTestCaseName(Launcher spoon, String indexTest, String fullQualifiedName) {
        return spoon.getFactory().Class().get(fullQualifiedName).getMethodsByName("test" + indexTest).isEmpty() ?
                (spoon.getFactory().Class().get(fullQualifiedName).getMethodsByName("test0" + indexTest).isEmpty() ?
                        "test00" + indexTest :
                        "test0" + indexTest
                ) : "test" + indexTest;
    }
}
