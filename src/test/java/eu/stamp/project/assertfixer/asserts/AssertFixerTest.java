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
                "@@ -43,1 +43,1 @@\n" +
                "-        Assert.assertTrue(ClassResourcesTest.getFalse());\n" +
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
                "@@ -48,1 +48,1 @@\n" +
                "-        Assert.assertEquals(32, ClassResourcesTest.getInt());\n" +
                "+        Assert.assertEquals(23, ClassResourcesTest.getInt());";

        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testFixAssertionArray() throws Exception {
        AssertFixerResult result = test("testAssertionErrorArray");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -53,3 +53,3 @@\n" +
                "-        Assert.assertArrayEquals(new int[]{ 32, 23 }, ClassResourcesTest.getArrayInt());\n" +
                "-        Assert.assertArrayEquals(new long[]{ 32L, 23L }, ClassResourcesTest.getArrayLong());\n" +
                "-        Assert.assertArrayEquals(new double[]{ 32.0, 23.0 }, ClassResourcesTest.getArrayDouble(), 0.05);\n" +
                "+        Assert.assertArrayEquals(new int[]{ 23,32}, ClassResourcesTest.getArrayInt());\n" +
                "+        Assert.assertArrayEquals(new long[]{ 23,32}, ClassResourcesTest.getArrayLong());\n" +
                "+        Assert.assertArrayEquals(new double[]{ 23.0,32.0}, ClassResourcesTest.getArrayDouble(), 0.05);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAddTryCatchFailBlock() throws Exception {
        AssertFixerResult result = test("testAddTryCatchFail");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -60,1 +60,6 @@\n" +
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
                "@@ -68,2 +68,2 @@\n" +
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
                "@@ -78,4 +78,4 @@\n" +
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
                "@@ -106,2 +106,2 @@\n" +
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
                "@@ -99,1 +99,6 @@\n" +
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
                "@@ -117,1 +117,1 @@\n" +
                "-        Assert.assertEquals(0.0, ClassResourcesTest.getNaN(), 0.0);\n" +
                "+        Assert.assertEquals(Double.NaN, ClassResourcesTest.getNaN(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testPositiveInfinity() throws Exception {
        AssertFixerResult result = test("testPositiveInfinity");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -122,1 +122,1 @@\n" +
                "-        Assert.assertEquals(Double.NEGATIVE_INFINITY, ClassResourcesTest.getInfinityPositive(), 0.0);\n" +
                "+        Assert.assertEquals(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityPositive(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testNegativeInfinity() throws Exception {
        AssertFixerResult result = test("testNegativeInfinity");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -127,1 +127,1 @@\n" +
                "-        Assert.assertEquals(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityNegative(), 0.0);\n" +
                "+        Assert.assertEquals(Double.NEGATIVE_INFINITY, ClassResourcesTest.getInfinityNegative(), 0.0);";
        assertEquals(expectedDiff, result.getDiff());
    }

    @Test
    public void testAssertSame() throws Exception {
        AssertFixerResult result = test("testAssertSame");
        assertNotNull(result.getDiff());
        String expectedDiff = "--- src/test/resources/ClassResourcesTest.java\n" +
                "+++ src/test/resources/ClassResourcesTest.java\n" +
                "@@ -133,1 +133,1 @@\n" +
                "-        Assert.assertSame(Double.POSITIVE_INFINITY, ClassResourcesTest.getInfinityNegative());\n" +
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
                "@@ -90,1 +90,1 @@\n" +
                "-        try {\n" +
                "+        {\n" +
                "@@ -92,2 +92,0 @@\n" +
                "-            Assert.fail(\"Expecting exception: IndexOutOfBoundsException\");\n" +
                "-        } catch (IndexOutOfBoundsException e) {";

        assertEquals(expectedDiff, result.getDiff());
    }
}
