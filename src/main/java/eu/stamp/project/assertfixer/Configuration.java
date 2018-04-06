package eu.stamp.project.assertfixer;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Configuration {
    private String classpath;
    private String fullQualifiedFailingTestClass;
    private List<String> failingTestMethods;
    private String pathToSourceFolder;
    private String pathToTestFolder;
    private boolean verbose;
    private String output;

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

    public String getPathToSourceFolder() {
        return pathToSourceFolder;
    }

    public void setPathToSourceFolder(String pathToSourceFolder) {
        this.pathToSourceFolder = pathToSourceFolder;
    }

    public String getPathToTestFolder() {
        return pathToTestFolder;
    }

    public void setPathToTestFolder(String pathToTestFolder) {
        this.pathToTestFolder = pathToTestFolder;
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
}
