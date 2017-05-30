package fr.inria.spirals.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runner.notification.Failure;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/05/17
 */
public class TestRunnerTest {

    private Launcher spoon;
    private SpoonModelBuilder compiler;

    @Before
    public void setUp() throws Exception {
        spoon = new Launcher();
        spoon.addInputResource("src/test/resources/ClassResourcesTest.java");
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.run();
        compiler = spoon.createCompiler();
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
    }

    @Test
    public void testRunTestClass() throws Exception {
        final List<Failure> failures =
                TestRunner.runTest("aPackage.ClassResourcesTest", new String[]{"spooned-classes/"});
        assertEquals(5, failures.size());
        assertEquals(2, failures.stream().filter(failure -> failure.getException() instanceof NullPointerException).count());
        assertEquals(3, failures.stream().filter(failure -> failure.getException() instanceof AssertionError).count());//The ArrayComparisonFailure is a subclass of AssertionError
        assertEquals(1, failures.stream().filter(failure -> failure.getException() instanceof ArrayComparisonFailure).count());
    }

    @Test
    public void testRunTestCase() throws Exception {
        List<Failure> failures =
                TestRunner.runTest("aPackage.ClassResourcesTest",
                        "testAddTryCatchFail",
                        new String[]{"spooned-classes/"});

        assertEquals(1, failures.size());
        assertTrue(failures.get(0).getException() instanceof NullPointerException);

        failures = TestRunner.runTest("aPackage.ClassResourcesTest",
                        "testAssertionErrorPrimitive",
                        new String[]{"spooned-classes/"});

        assertEquals(1, failures.size());
        assertTrue(failures.get(0).getException() instanceof AssertionError);

        failures = TestRunner.runTest("aPackage.ClassResourcesTest",
                "testRemoveTryCatchBlock",
                new String[]{"spooned-classes/"});

        assertEquals(1, failures.size());
        assertTrue(failures.get(0).getException() instanceof AssertionError);

        failures = TestRunner.runTest("aPackage.ClassResourcesTest",
                "testReplaceExpectedException",
                new String[]{"spooned-classes/"});

        assertEquals(1, failures.size());
        assertTrue(failures.get(0).getException() instanceof NullPointerException);

        failures = TestRunner.runTest("aPackage.ClassResourcesTest",
                "testAssertionErrorArray",
                new String[]{"spooned-classes/"});

        assertEquals(1, failures.size());
        assertTrue(failures.get(0).getException() instanceof AssertionError);
        assertTrue(failures.get(0).getException() instanceof ArrayComparisonFailure);
    }
}
