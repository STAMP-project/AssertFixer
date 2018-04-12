package eu.stamp.project.assertfixer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Configuration {
    private String classpath;
    private String fullQualifiedFailingTestClass;
    private List<String> failingTestMethods;
    private List<String> pathToSourceFolder;
    private List<String> pathToTestFolder;
    private boolean verbose;
    private String output;
    private Map<String, List<String>> multipleTestCases;

    public String getSourceOutputDirectory() {
        return this.output + "/spooned";
    }

    public String getBinaryOutputDirectory() {
        return this.output + "/spooned-classes/";
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public String getFullQualifiedFailingTestClass() {
        return fullQualifiedFailingTestClass;
    }

    public void setFullQualifiedFailingTestClass(String fullQualifiedFailingTestClass) {
        this.fullQualifiedFailingTestClass = fullQualifiedFailingTestClass;
    }

    public List<String> getFailingTestMethods() {
        return failingTestMethods;
    }

    public void setFailingTestMethods(List<String> failingTestMethods) {
        this.failingTestMethods = failingTestMethods;
    }

    public List<String> getPathToSourceFolder() {
        return pathToSourceFolder;
    }

    public void setPathToSourceFolder(List<String> pathToSourceFolder) {
        for (String path : pathToSourceFolder) {
            if (!new File(path).exists()) {
                throw new RuntimeException("All paths to source folder must be existing", new FileNotFoundException(path+" does not exist."));
            }
        }

        this.pathToSourceFolder = new ArrayList<>(pathToSourceFolder);
    }

    public List<String> getPathToTestFolder() {
        return pathToTestFolder;
    }

    public void setPathToTestFolder(List<String> pathToTestFolder) {
        for (String path : pathToTestFolder) {
            if (!new File(path).exists()) {
                throw new RuntimeException("All paths to test folder must be existing", new FileNotFoundException(path+" does not exist."));
            }
        }

        this.pathToTestFolder = new ArrayList<>(pathToTestFolder);
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Map<String, List<String>> getMultipleTestCases() {
        return multipleTestCases;
    }

    public void setMultipleTestCases(Map<String, List<String>> multipleTestCases) {
        this.multipleTestCases = new HashMap<>(multipleTestCases);
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "classpath='" + classpath + '\'' +
                ", fullQualifiedFailingTestClass='" + fullQualifiedFailingTestClass + '\'' +
                ", failingTestMethods=" + failingTestMethods +
                ", pathToSourceFolder='" + pathToSourceFolder + '\'' +
                ", pathToTestFolder='" + pathToTestFolder + '\'' +
                ", verbose=" + verbose +
                ", output='" + output + '\'' +
                '}';
    }
}
