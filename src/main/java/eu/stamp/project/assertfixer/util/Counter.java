package eu.stamp.project.assertfixer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/05/17
 */
public class Counter {

    private static Counter _instance;

    private static Counter _getInstance() {
        if (_instance == null) {
            _instance = new Counter();
        }
        return _instance;
    }

    private Counter() {
        this.initialNumberOfTest = 0;
        this.initialNumberOfFailingTestFromException = 0;
        this.initialNumbernumberOfFailingTestFromAssertion = 0;
        this.distributionOfNumberOfAssertionsByTests = new ArrayList<>();
        this.numberOfFixedTests = 0;
        this.numberOfNotFixedTests = 0;
        this.numberOfPrimitivesValuesFixed = 0;
        this.numberOfArrayFixed = 0;
        this.numberOfFailingAssertionInTests = new ArrayList<>();
    }

    private int initialNumberOfTest;

    private int initialNumberOfFailingTestFromException;
    private int initialNumbernumberOfFailingTestFromAssertion;

    private List<Integer> distributionOfNumberOfAssertionsByTests;

    private int numberOfFixedTests;
    private int numberOfNotFixedTests;

    private int numberOfPrimitivesValuesFixed;
    private int numberOfArrayFixed;

    private int numberOfFailingAssertion = 0;
    private List<Integer> numberOfFailingAssertionInTests;

    public static void incNumberOfTest() {
        _getInstance().initialNumberOfTest++;
    }

    public static void incNumberOfFailingTestFromException() {
        _getInstance().initialNumberOfFailingTestFromException++;
    }

    public static void incNumberOfFailingTestFromAssertion() {
        _getInstance().initialNumbernumberOfFailingTestFromAssertion++;
    }

    public static void addNumberOfAssertionInTests(Integer number) {
        _getInstance().distributionOfNumberOfAssertionsByTests.add(number);
    }

    public static void incNumberOfFailingAssertion() {
        _getInstance().numberOfFailingAssertion++;
    }

    public static void updateNumberOfFailingAssertionInTests() {
        _getInstance().numberOfFailingAssertionInTests.add(_getInstance().numberOfFailingAssertion);
        _getInstance().numberOfFailingAssertion = 0;
    }

    public static void incNumberOfFixedTests() {
        _getInstance().numberOfFixedTests++;
    }

    public static void incNumberOfNotFixedTests() {
        _getInstance().numberOfNotFixedTests++;
    }

    public static void incNumberOfPrimitivesValuesFixed() {
        _getInstance().numberOfPrimitivesValuesFixed++;
    }

    public static void incNumberOfArrayFixed() {
        _getInstance().numberOfArrayFixed++;
    }

    public static void reset() {
        _instance = null;
    }

    public static void print() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!new File("output").exists()) {
            new File("output").mkdir();
        }
        try (FileWriter writer = new FileWriter(new File("output" + Util.FILE_SEPARATOR + "metrics.json"), false)) {
            JSONMetrics jsonMetrics = new JSONMetrics(
                    _getInstance().initialNumberOfTest,
                    _getInstance().initialNumberOfFailingTestFromException + _getInstance().initialNumbernumberOfFailingTestFromAssertion,
                    _getInstance().initialNumberOfFailingTestFromException,
                    _getInstance().initialNumbernumberOfFailingTestFromAssertion,
                    _getInstance().distributionOfNumberOfAssertionsByTests.stream().map(Object::toString).collect(Collectors.joining(",")),
                    _getInstance().numberOfFixedTests,
                    _getInstance().numberOfNotFixedTests,
                    _getInstance().numberOfPrimitivesValuesFixed,
                    _getInstance().numberOfArrayFixed,
                    _getInstance().numberOfFailingAssertionInTests.stream().map(Object::toString).collect(Collectors.joining(","))
            );
            writer.write(gson.toJson(jsonMetrics));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class JSONMetrics {
        public final int initialNumberOfTest;
        public final int initialNumberOfFailingTest;
        public final int initialNumberOfFailingTestFromException;
        public final int initialNumberOfFailingTestFromAssertion;
        public final String distributionOfNumberOfAssertionsByTests;
        public final int numberOfFixedTests;
        public final int numberOfNotFixedTests;
        public final int numberOfPrimitivesValuesFixed;
        public final int numberOfArrayFixed;
        public final String numberOfFailingAssertionInTests;
        public JSONMetrics(int initialNumberOfTest, int initialNumberOfFailingTest, int initialNumberOfFailingTestFromException, int initialNumberOfFailingTestFromAssertion, String distributionOfNumberOfAssertionsByTests, int numberOfFixedTests, int numberOfNotFixedTests, int numberOfPrimitivesValuesFixed, int numberOfArrayFixed, String numberOfFailingAssertionInTests) {
            this.initialNumberOfTest = initialNumberOfTest;
            this.initialNumberOfFailingTest = initialNumberOfFailingTest;
            this.initialNumberOfFailingTestFromException = initialNumberOfFailingTestFromException;
            this.initialNumberOfFailingTestFromAssertion = initialNumberOfFailingTestFromAssertion;
            this.distributionOfNumberOfAssertionsByTests = distributionOfNumberOfAssertionsByTests;
            this.numberOfFixedTests = numberOfFixedTests;
            this.numberOfNotFixedTests = numberOfNotFixedTests;
            this.numberOfPrimitivesValuesFixed = numberOfPrimitivesValuesFixed;
            this.numberOfArrayFixed = numberOfArrayFixed;
            this.numberOfFailingAssertionInTests = numberOfFailingAssertionInTests;
        }
    }
}
