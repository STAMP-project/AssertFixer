package eu.stamp.project.assertfixer;

public class AssertFixerResult {
    private String testClass;
    private String testMethod;
    private boolean success;
    private String exceptionMessage;
    private String diff;


    public AssertFixerResult(String testClass, String testMethod) {
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }
}
