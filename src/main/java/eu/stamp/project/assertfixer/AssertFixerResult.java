package eu.stamp.project.assertfixer;

public class AssertFixerResult {

    public enum RepairType {NoRepair, AssertRepair, TryCatchRepair, RemoveException};

    private String testClass;
    private String testMethod;
    private boolean success;
    private String exceptionMessage;
    private String diff;
    private String filePath;
    private RepairType type;


    public AssertFixerResult(String testClass, String testMethod) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.type = RepairType.NoRepair;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public RepairType getRepairType() {
        return type;
    }

    public void setRepairType(RepairType type) {
        this.type = type;
    }
}
