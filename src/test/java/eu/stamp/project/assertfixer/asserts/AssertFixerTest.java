package eu.stamp.project.assertfixer.asserts;

import eu.stamp.project.assertfixer.AbstractTest;
import eu.stamp.project.testrunner.EntryPoint;
import eu.stamp.project.testrunner.runner.test.Failure;
import org.junit.Test;
import spoon.SpoonModelBuilder;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/05/17
 */
public class AssertFixerTest extends AbstractTest {

    private void test(String testCaseName) throws Exception {
        String fullQualifiedName = "aPackage.ClassResourcesTest";

        List<Failure> failures = EntryPoint.runTests(
                getClasspath(),
                fullQualifiedName,
                testCaseName).getFailingTests();// 1st assert fail

        assertTrue(failures.size() == 1);

        AssertFixer.fixAssert(spoon,
                fullQualifiedName,
                testCaseName,
                failures.get(0),
                getClasspath());

        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        failures = EntryPoint.runTests(
                getClasspath(),
                fullQualifiedName,
                testCaseName).getFailingTests();

        assertTrue("should have been empty " + failures ,failures.isEmpty());
    }

    @Test
    public void testFixAssertionBoolean() throws Exception {
        test("testAssertionErrorBoolean");
    }

    @Test
    public void testFixAssertionPrimitive() throws Exception {
        test("testAssertionErrorPrimitive");
    }

    @Test
    public void testFixAssertionArray() throws Exception {
        test("testAssertionErrorArray");
    }

    @Test
    public void testAddTryCatchFailBlock() throws Exception {
        test("testAddTryCatchFail");
    }

    @Test
    public void testReplaceExpectedException() throws Exception {
        test("testReplaceExpectedException");
    }

    @Test
    public void testReplaceExpectedExceptionWithForwardReference() throws Exception {
        test("testReplaceExpectedExceptionWithForwardReference");
    }

    @Test
    public void testReplaceExpectedExceptionAnonymous() throws Exception {
        test("testReplaceExpectedExceptionAnonymous");
    }

    @Test
    public void testAddExpectedExceptionAnonymous() throws Exception {
        test("testAddExpectedExceptionAnonymous");
    }

    @Test
    public void testAssertionErrorField() throws Exception {
        test("testAssertionErrorField");
    }

    @Test
    public void testPositiveInfinity() throws Exception {
        test("testPositiveInfinity");
    }

    @Test
    public void testNegativeInfinity() throws Exception {
        test("testNegativeInfinity");
    }

    @Test
    public void testAssertSame() throws Exception {
        test("testAssertSame");
    }

    //TODO check that this the behavior we want,
    //TODO it is based on convetion (message of fail() is starting with "Expecting exception: " and endsWith "Exception".
    @Test
    public void testRemoveTryCatchBlock() throws Exception {
        test("testRemoveTryCatchBlock");
    }
}
