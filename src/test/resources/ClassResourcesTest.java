package aPackage;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClassResourcesTest {

    private static int getInt() {
        return 23;
    }

    private static int[] getArrayInt() {
        return new int[] {23,32};
    }

    private static void throwNPE() {
        throw new NullPointerException();
    }

    private static void doNotThrowNPE() {
        //empty
    }

    @Test
    public void testAssertionErrorPrimitive() {
        assertEquals(32, getInt());
    }

    @Test
    public void testAssertionErrorArray() {
        assertArrayEquals(new int[] {32,23}, getArrayInt());
    }

    @Test
    public void testAddTryCatchFail() {
        throwNPE();
    }

    @Test
    public void testReplaceExpectedException() {
        try {
            throwNPE();
            fail("");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testRemoveTryCatchBlock() {
        try {
            doNotThrowNPE();
            fail("Expecting exception: IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {

        }
    }

}