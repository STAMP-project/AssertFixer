package eu.stamp.util;

import eu.stamp.AbstractTest;
import eu.stamp.EntryPoint;
import eu.stamp.asserts.AssertFixer;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

import static eu.stamp.util.Util.FILE_SEPARATOR;
import static eu.stamp.util.Util.LINE_SEPARATOR;
import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/05/17
 */
public class CounterTest extends AbstractTest {

    private void test(String testCaseName) throws Exception {
        String fullQualifiedName = "aPackage.ClassResourcesTest";
        List<eu.stamp.runner.test.Failure> failures = EntryPoint.runTests(
                getClasspath(),
                fullQualifiedName,
                testCaseName).getFailingTests();// 1st assert fail

        AssertFixer.fixAssert(spoon,
                fullQualifiedName,
                testCaseName,
                failures.get(0),
                getClasspath());
    }

    @Test
    public void testCounter() throws Exception {
        Counter.reset();
        test("testAssertionErrorBoolean");
        test("testAssertionErrorPrimitive");
        test("testAssertionErrorArray");
        test("testAddTryCatchFail");
        test("testReplaceExpectedException");
        test("testRemoveTryCatchBlock");
        Counter.print();
        try (BufferedReader reader = new BufferedReader(new FileReader("output" + FILE_SEPARATOR + "metrics.json"))) {
            String jsonAsString = reader.lines().collect(Collectors.joining(LINE_SEPARATOR));
            assertEquals(expectedJson, jsonAsString);
        }
    }

    private static final String expectedJson = "{" + LINE_SEPARATOR +
            "  \"initialNumberOfTest\": 0," + LINE_SEPARATOR +
            "  \"initialNumberOfFailingTest\": 6," + LINE_SEPARATOR +
            "  \"initialNumberOfFailingTestFromException\": 2," + LINE_SEPARATOR +
            "  \"initialNumberOfFailingTestFromAssertion\": 4," + LINE_SEPARATOR +
            "  \"distributionOfNumberOfAssertionsByTests\": \"1,1,3,0,0,0\"," + LINE_SEPARATOR +
            "  \"numberOfFixedTests\": 0," + LINE_SEPARATOR +
            "  \"numberOfNotFixedTests\": 0," + LINE_SEPARATOR +
            "  \"numberOfPrimitivesValuesFixed\": 1," + LINE_SEPARATOR +
            "  \"numberOfArrayFixed\": 3," + LINE_SEPARATOR +
            "  \"numberOfFailingAssertionInTests\": \"\"" + LINE_SEPARATOR +
            "}";

}
