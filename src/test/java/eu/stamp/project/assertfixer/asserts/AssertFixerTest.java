package eu.stamp.project.assertfixer.asserts;

import eu.stamp.project.assertfixer.AbstractTest;
import eu.stamp.project.assertfixer.AssertFixerResult;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.Test;
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

        assertEquals(1, failures.size());
        CtClass testClass = spoon.getFactory().Class().get(fullQualifiedName);
        AssertFixerResult result = AssertFixer.fixAssert(configuration, spoon,
                testClass,
                testCaseName,
                failures.get(0),
                getClasspath());

        assertTrue("result should have been successful " + result.getExceptionMessage(), result.isSuccess());
        return result;
    }

    @Test
    public void testFixAssertionBoolean() throws Exception {
        AssertFixerResult result = test("testAssertionErrorBoolean");

        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -44,1 +44,2 @@\n" +
                "-        assertTrue(getFalse());\n" +
                "+        // AssertFixer: old assertion assertTrue(getFalse())\n" +
                "+        org.junit.Assert.assertFalse(getFalse());";
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
                "-        assertEquals(32, getInt());\n" +
                "+        // AssertFixer: old assertion assertEquals(32, getInt())\n" +
                "+        assertEquals(23, getInt());";

        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testFixAssertionArray() throws Exception {
        AssertFixerResult result = test("testAssertionErrorArray");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -54,3 +54,6 @@\n" +
                "-        assertArrayEquals(new int[]{ 32, 23 }, getArrayInt());\n" +
                "-        assertArrayEquals(new long[]{ 32L, 23L }, getArrayLong());\n" +
                "-        assertArrayEquals(new double[]{ 32.0, 23.0 }, getArrayDouble(), 0.05);\n" +
                "+        // AssertFixer: old assertion assertArrayEquals(new int[]{ 32, 23 }, getArrayInt())\n" +
                "+        assertArrayEquals(new int[]{ 23,32}, getArrayInt());\n" +
                "+        // AssertFixer: old assertion assertArrayEquals(new long[]{ 32L, 23L }, getArrayLong())\n" +
                "+        assertArrayEquals(new long[]{ 23,32}, getArrayLong());\n" +
                "+        // AssertFixer: old assertion assertArrayEquals(new double[]{ 32.0, 23.0 }, getArrayDouble(), 0.05)\n" +
                "+        assertArrayEquals(new double[]{ 23.0,32.0}, getArrayDouble(), 0.05);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAddTryCatchFailBlock() throws Exception {
        AssertFixerResult result = test("testAddTryCatchFail");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -61,1 +61,6 @@\n" +
                "-        throwNPE();\n" +
                "+        try {\n" +
                "+            throwNPE();\n" +
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
                "-            fail(\"Expecting exception: IndexOutOfBoundsException\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {\n" +
                "+            fail(\"Expecting exception: NullPointerException\");\n" +
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
                "-            fail(\"Expecting exception: IndexOutOfBoundsException\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {\n" +
                "-            e.printStackTrace();\n" +
                "-            uselessMethod(e);\n" +
                "+            fail(\"Expecting exception: NullPointerException\");\n" +
                "+        } catch (NullPointerException expectedException__NullPointerException) {\n" +
                "+            expectedException__NullPointerException.printStackTrace();\n" +
                "+            uselessMethod(expectedException__NullPointerException);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testReplaceExpectedExceptionAnonymous() throws Exception {
        AssertFixerResult result = test("testReplaceExpectedExceptionAnonymous");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -108,2 +108,2 @@\n" +
                "-            fail(\"Expecting exception: Exception\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {\n" +
                "+            fail(\"Expecting exception: RuntimeException\");\n" +
                "+        } catch (RuntimeException expectedException__RuntimeException) {";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAddExpectedExceptionAnonymous() throws Exception {
        AssertFixerResult result = test("testAddExpectedExceptionAnonymous");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -101,1 +101,6 @@\n" +
                "-        throwAnonymous();\n" +
                "+        try {\n" +
                "+            throwAnonymous();\n" +
                "+            org.junit.Assert.fail(\"Expecting exception: RuntimeException\");\n" +
                "+        } catch (RuntimeException expectedException__RuntimeException) {\n" +
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
                "-        assertEquals(0.0, getNaN(), 0.0);\n" +
                "+        // AssertFixer: old assertion assertEquals(0.0, getNaN(), 0.0)\n" +
                "+        assertEquals(Double.NaN, getNaN(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testPositiveInfinity() throws Exception {
        AssertFixerResult result = test("testPositiveInfinity");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -124,1 +124,2 @@\n" +
                "-        assertEquals(Double.NEGATIVE_INFINITY, getInfinityPositive(), 0.0);\n" +
                "+        // AssertFixer: old assertion assertEquals(Double.NEGATIVE_INFINITY, getInfinityPositive(), 0.0)\n" +
                "+        assertEquals(Double.POSITIVE_INFINITY, getInfinityPositive(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testNegativeInfinity() throws Exception {
        AssertFixerResult result = test("testNegativeInfinity");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -129,1 +129,2 @@\n" +
                "-        assertEquals(Double.POSITIVE_INFINITY, getInfinityNegative(), 0.0);\n" +
                "+        // AssertFixer: old assertion assertEquals(Double.POSITIVE_INFINITY, getInfinityNegative(), 0.0)\n" +
                "+        assertEquals(Double.NEGATIVE_INFINITY, getInfinityNegative(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAssertSame() throws Exception {
        AssertFixerResult result = test("testAssertSame");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -134,1 +134,2 @@\n" +
                "-        assertSame(Double.POSITIVE_INFINITY, getInfinityNegative());\n" +
                "+        // AssertFixer: old assertion assertSame(Double.POSITIVE_INFINITY, getInfinityNegative())\n" +
                "+        assertNotSame(Double.POSITIVE_INFINITY, getInfinityNegative());";
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
                "-            fail(\"Expecting exception: IndexOutOfBoundsException\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {";

        assertEquals(expectedDiff, result.getDiff());
    }
}
