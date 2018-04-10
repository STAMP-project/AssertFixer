package eu.stamp.project.assertfixer.asserts;

import eu.stamp.project.assertfixer.AbstractTest;
import eu.stamp.project.assertfixer.AssertFixerResult;
import eu.stamp.project.testrunner.EntryPoint;
import eu.stamp.project.testrunner.runner.test.Failure;
import org.junit.Test;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtClass;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/05/17
 */
public class AssertFixerTest extends AbstractTest {

    private AssertFixerResult test(String testCaseName) throws Exception {
        String fullQualifiedName = "aPackage.ClassResourcesTest";

        List<Failure> failures = EntryPoint.runTests(
                getClasspath(),
                fullQualifiedName,
                testCaseName).getFailingTests();// 1st assert fail

        assertTrue(failures.size() == 1);
        CtClass testClass = spoon.getFactory().Class().get(fullQualifiedName);
        AssertFixerResult result = AssertFixer.fixAssert(configuration, spoon,
                testClass,
                testCaseName,
                failures.get(0),
                getClasspath());

        assertTrue("result should have been successful", result.isSuccess());
        return result;
    }

    @Test
    public void testFixAssertionBoolean() throws Exception {
        AssertFixerResult result = test("testAssertionErrorBoolean");

        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -44,1 +44,2 @@\n" +
                "-        Assert.assertTrue(ClassResourcesTest.getFalse());\n" +
                "+        // AssertFixer: old assertion Assert.assertTrue(ClassResourcesTest.getFalse())\n" +
                "+        Assert.assertFalse(ClassResourcesTest.getFalse());";
        assertNotNull(result.getDiff());
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testFixAssertionPrimitive() throws Exception {
        AssertFixerResult result = test("testAssertionErrorPrimitive");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -49,1 +49,2 @@\n" +
                "-        Assert.assertEquals(32, ClassResourcesTest.getInt());\n" +
                "+        // AssertFixer: old assertion Assert.assertEquals(32, ClassResourcesTest.getInt())\n" +
                "+        Assert.assertEquals(23, ClassResourcesTest.getInt());";

        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testFixAssertionArray() throws Exception {
        AssertFixerResult result = test("testAssertionErrorArray");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -54,3 +54,6 @@\n" +
                "-        Assert.assertArrayEquals(new int[]{ 32, 23 }, ClassResourcesTest.getArrayInt());\n" +
                "-        Assert.assertArrayEquals(new long[]{ 32L, 23L }, ClassResourcesTest.getArrayLong());\n" +
                "-        Assert.assertArrayEquals(new double[]{ 32.0, 23.0 }, ClassResourcesTest.getArrayDouble(), 0.05);\n" +
                "+        // AssertFixer: old assertion Assert.assertArrayEquals(new int[]{ 32, 23 }, ClassResourcesTest.getArrayInt())\n" +
                "+        Assert.assertArrayEquals(new int[]{ 23,32}, ClassResourcesTest.getArrayInt());\n" +
                "+        // AssertFixer: old assertion Assert.assertArrayEquals(new long[]{ 32L, 23L }, ClassResourcesTest.getArrayLong())\n" +
                "+        Assert.assertArrayEquals(new long[]{ 23,32}, ClassResourcesTest.getArrayLong());\n" +
                "+        // AssertFixer: old assertion Assert.assertArrayEquals(new double[]{ 32.0, 23.0 }, ClassResourcesTest.getArrayDouble(), 0.05)\n" +
                "+        Assert.assertArrayEquals(new double[]{ 23.0,32.0}, ClassResourcesTest.getArrayDouble(), 0.05);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAddTryCatchFailBlock() throws Exception {
        AssertFixerResult result = test("testAddTryCatchFail");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -61,1 +61,6 @@\n" +
                "-        ClassResourcesTest.throwNPE();\n" +
                "+        try {\n" +
                "+            ClassResourcesTest.throwNPE();\n" +
                "+            org.junit.Assert.fail(\"Expecting exception: NullPointerException\");\n" +
                "+        } catch (NullPointerException expectedException__NullPointerException) {\n" +
                "+            org.junit.Assert.assertTrue(true);\n" +
                "+        }";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testReplaceExpectedException() throws Exception {
        AssertFixerResult result = test("testReplaceExpectedException");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -69,2 +69,2 @@\n" +
                "-            Assert.fail(\"Expecting exception: IndexOutOfBoundsException\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {\n" +
                "+            Assert.fail(\"Expecting exception: NullPointerException\");\n" +
                "+        } catch (NullPointerException expectedException__NullPointerException) {";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testReplaceExpectedExceptionWithForwardReference() throws Exception {
        AssertFixerResult result = test("testReplaceExpectedExceptionWithForwardReference");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -79,4 +79,4 @@\n" +
                "-            Assert.fail(\"Expecting exception: IndexOutOfBoundsException\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {\n" +
                "-            e.printStackTrace();\n" +
                "-            ClassResourcesTest.uselessMethod(e);\n" +
                "+            Assert.fail(\"Expecting exception: NullPointerException\");\n" +
                "+        } catch (NullPointerException expectedException__NullPointerException) {\n" +
                "+            expectedException__NullPointerException.printStackTrace();\n" +
                "+            ClassResourcesTest.uselessMethod(expectedException__NullPointerException);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testReplaceExpectedExceptionAnonymous() throws Exception {
        AssertFixerResult result = test("testReplaceExpectedExceptionAnonymous");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -108,2 +108,2 @@\n" +
                "-            Assert.fail(\"Expecting exception: Exception\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {\n" +
                "+            Assert.fail(\"Expecting exception: ClassResourcesTest$1\");\n" +
                "+        } catch (RuntimeException expectedException__ClassResourcesTest$1) {";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAddExpectedExceptionAnonymous() throws Exception {
        AssertFixerResult result = test("testAddExpectedExceptionAnonymous");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -101,1 +101,6 @@\n" +
                "-        ClassResourcesTest.throwAnonymous();\n" +
                "+        try {\n" +
                "+            ClassResourcesTest.throwAnonymous();\n" +
                "+            org.junit.Assert.fail(\"Expecting exception: ClassResourcesTest$1\");\n" +
                "+        } catch (RuntimeException expectedException__ClassResourcesTest$1) {\n" +
                "+            org.junit.Assert.assertTrue(true);\n" +
                "+        }";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAssertionErrorField() throws Exception {
        AssertFixerResult result = test("testAssertionErrorField");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -119,1 +119,2 @@\n" +
                "-        Assert.assertEquals(0.0, ClassResourcesTest.getNaN(), 0.0);\n" +
                "+        // AssertFixer: old assertion Assert.assertEquals(0.0, ClassResourcesTest.getNaN(), 0.0)\n" +
                "+        Assert.assertEquals(Double.NaN, ClassResourcesTest.getNaN(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testPositiveInfinity() throws Exception {
        AssertFixerResult result = test("testPositiveInfinity");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -124,1 +124,2 @@\n" +
                "-        Assert.assertEquals(Double.NEGATIVE_INFINITY, ClassResourcesTest.getInfinityPositive(), 0.0);\n" +
                "+        // AssertFixer: old assertion Assert.assertEquals(Double.NEGATIVE_INFINITY, ClassResourcesTest.getInfinityPositive(), 0.0)\n" +
                "+        Assert.assertEquals(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityPositive(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testNegativeInfinity() throws Exception {
        AssertFixerResult result = test("testNegativeInfinity");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -129,1 +129,2 @@\n" +
                "-        Assert.assertEquals(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityNegative(), 0.0);\n" +
                "+        // AssertFixer: old assertion Assert.assertEquals(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityNegative(), 0.0)\n" +
                "+        Assert.assertEquals(Double.NEGATIVE_INFINITY, ClassResourcesTest.getInfinityNegative(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAssertSame() throws Exception {
        AssertFixerResult result = test("testAssertSame");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -134,0 +134,1 @@\n" +
                "+        // AssertFixer: old assertion Assert.assertNotSame(Double.NEGATIVE_INFINITY, ClassResourcesTest.getInfinityNegative())\n" +
                "@@ -135,1 +136,2 @@\n" +
                "-        Assert.assertSame(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityNegative());\n" +
                "+        // AssertFixer: old assertion Assert.assertSame(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityNegative())\n" +
                "+        Assert.assertNotSame(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityNegative());";
        assertEquals(expectedDiff, result.getDiff());
    }

    //TODO check that this the behavior we want,
    //TODO it is based on convetion (message of fail() is starting with "Expecting exception: " and endsWith "Exception".
    @Test
    public void testRemoveTryCatchBlock() throws Exception {
        AssertFixerResult result = test("testRemoveTryCatchBlock");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -92,1 +92,1 @@\n" +
                "-        try {\n" +
                "+        {\n" +
                "@@ -94,2 +94,0 @@\n" +
                "-            Assert.fail(\"Expecting exception: IndexOutOfBoundsException\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {";

        assertEquals(expectedDiff, result.getDiff());
    }
}
