package fr.inria.spirals.asserts;

import fr.inria.spirals.test.TestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

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

    @Before
    public void setUp() throws Exception {
        spoon = new Launcher();
        spoon.addInputResource("src/test/resources/ClassResourcesTest.java");
        spoon.addInputResource("src/main/java/fr/inria/spirals/asserts/log/Logger.java"); // adding the logger to the spoon model to compile and run it to fix assertions
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.run();
        compiler = spoon.createCompiler();
    }

    private void test(String testCaseName) throws Exception  {
        String fullQualifiedName = "aPackage.ClassResourcesTest";
        List<Failure> failures = TestRunner.runTest(
                fullQualifiedName,
                testCaseName,
                new String[]{"spooned-classes/"});// 1st assert fail

        assertTrue(failures.size() == 1);

        AssertFixer.fixAssert(spoon,
                fullQualifiedName,
                testCaseName,
                failures.get(0),
                "spooned-classes/");

        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        failures = TestRunner.runTest(
                fullQualifiedName,
                testCaseName,
                new String[]{"spooned-classes/"});// repaired

        assertTrue(failures.isEmpty());
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

    //TODO check that this the behavior we want,
    //TODO it is based on convetion (message of fail() is starting with "Expecting exception: " and endsWith "Exception".
    @Test
    public void testRemoveTryCatchBlock() throws Exception {
        test("testRemoveTryCatchBlock");
    }
}
