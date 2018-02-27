package eu.stamp.json;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/05/17
 */
public class JSONTest {

    public final String name;
    public final boolean adaptationSuccessful;
    public final String remainingFailures;
    public final boolean noObservedFailureOnOriginalTest;
    public final boolean timeoutBeforeAdaptation;

    public JSONTest(String name, boolean adaptationSuccessful, String remainingFailures, boolean noObservedFailureOnOriginalTest, boolean timeoutBeforeAdaptation) {
        this.name = name;
        this.adaptationSuccessful = adaptationSuccessful;
        this.remainingFailures = remainingFailures;
        this.noObservedFailureOnOriginalTest = noObservedFailureOnOriginalTest;
        this.timeoutBeforeAdaptation = timeoutBeforeAdaptation;
    }
}
