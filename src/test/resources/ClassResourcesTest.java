package aPackage;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClassResourcesTest {

    private static boolean getFalse() {
        return false;
    }

    private static int getInt() {
        return 23;
    }

    private static int[] getArrayInt() {
        return new int[]{23, 32};
    }

    private static long[] getArrayLong() {
        return new long[]{23L, 32L};
    }

    private static double[] getArrayDouble() {
        return new double[]{23.0D, 32.0D};
    }

    private static void throwNPE() {
        throw new NullPointerException();
    }

    private static void doNotThrowNPE() {
        //empty
    }

    private static void throwAnonymous() {
        throw new RuntimeException() {

        };
    }

    private static Double getInfinityPositive() {
        return Double.POSITIVE_INFINITY;
    }

    private static Double getInfinityNegative() {
        return Double.NEGATIVE_INFINITY;
    }

    @Test
    public void testAssertionErrorBoolean() {
        assertTrue(getFalse());
    }

    @Test
    public void testAssertionErrorPrimitive() {
        assertEquals(32, getInt());
    }

    @Test
    public void testAssertionErrorArray() {
        assertArrayEquals(new int[]{32, 23}, getArrayInt());
        assertArrayEquals(new long[]{32L, 23L}, getArrayLong());
        assertArrayEquals(new double[]{32.0D, 23.0D}, getArrayDouble(), 0.05D);
    }

    @Test
    public void testAddTryCatchFail() {
        throwNPE();
    }

    @Test
    public void testReplaceExpectedException() {
        try {
            throwNPE();
            doNotThrowNPE();
            fail("Expecting exception: IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testReplaceExpectedExceptionWithForwardReference() {
        try {
            throwNPE();
            doNotThrowNPE();
            fail("Expecting exception: IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            uselessMethod(e);
        }
    }

    private static void uselessMethod(Throwable t) {
        //do nothing
    }

    @Test
    public void testRemoveTryCatchBlock() {
        try {
            doNotThrowNPE();
            fail("Expecting exception: IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testAddExpectedExceptionAnonymous() throws Exception {
        throwAnonymous();
    }

    @Test
    public void testReplaceExpectedExceptionAnonymous() throws Exception {
        try {
            throwAnonymous();
            fail("Expecting exception: Exception");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    private static Double getNaN() {
        return Double.NaN;
    }

    @Test
    public void testAssertionErrorField() throws Exception {
        assertEquals(0.0D, getNaN(), 0.0D);
    }

    @Test
    public void testPositiveInfinity() throws Exception {
        assertEquals(Double.NEGATIVE_INFINITY, getInfinityPositive(), 0.0D);
    }

    @Test
    public void testNegativeInfinity() throws Exception {
        assertEquals(Double.POSITIVE_INFINITY, getInfinityNegative(), 0.0D);
    }

    @Test
    public void testAssertSame() throws Exception {
        assertSame(Double.POSITIVE_INFINITY, getInfinityNegative());
    }

    static {
        foo.NotExist.foo();
    }
}