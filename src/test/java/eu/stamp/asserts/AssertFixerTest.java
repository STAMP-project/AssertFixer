package eu.stamp.asserts;

import eu.stamp.EntryPoint;
import eu.stamp.Main;
import eu.stamp.runner.test.Failure;
import org.hamcrest.BaseDescription;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/05/17
 */
public class AssertFixerTest {

    private Launcher spoon;
    private SpoonModelBuilder compiler;

    private static final String dependenciesToRunJUnit;

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


    @Before
    public void setUp() throws Exception {
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
    }

    private void test(String testCaseName) throws Exception {
        String fullQualifiedName = "aPackage.ClassResourcesTest";

        List<Failure> failures = EntryPoint.runTests(
                Main.configuration.getBinaryOutputDirectory() + ":" + dependenciesToRunJUnit,
                fullQualifiedName,
                testCaseName).getFailingTests();// 1st assert fail

        assertTrue(failures.size() == 1);

        AssertFixer.fixAssert(spoon,
                fullQualifiedName,
                testCaseName,
                failures.get(0),
                Main.configuration.getBinaryOutputDirectory() + ":" + dependenciesToRunJUnit);

        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        failures = EntryPoint.runTests(
                Main.configuration.getBinaryOutputDirectory() + ":" + dependenciesToRunJUnit,
                fullQualifiedName,
                testCaseName).getFailingTests();

        assertTrue("should have been empty " + failures ,failures.isEmpty());
    }

    @Test
    public void testFixAssertionBoolean() throws Exception {
        EntryPoint.verbose = true;
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
