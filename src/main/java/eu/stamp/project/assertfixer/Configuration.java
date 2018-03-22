package eu.stamp.project.assertfixer;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import eu.stamp.project.assertfixer.util.Util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Configuration {

    private static final JSAP jsap = initJSAP();

    public final String classpath;
    public final String fullQualifiedFailingTestClass;
    public final List<String> failingTestMethods;
    public final String pathToSourceFolder;
    public final String pathToTestFolder;
    public final boolean verbose;
    public final String output;

    private Configuration(String [] args) {
        final JSAPResult options = jsap.parse(args);
        if (options.getBoolean("help")) {
            usage();
        }
        this.classpath = options.getString("classpath");
        this.fullQualifiedFailingTestClass = options.getString("testClass");
        this.failingTestMethods = Arrays.asList(options.getString("testMethod").split(":"));
        this.pathToSourceFolder = options.getString("sourcePath");
        this.pathToTestFolder = options.getString("testPath");
        this.verbose = options.getBoolean("verbose");
        this.output = options.getString("output");
    }

    public static Configuration get(String[] args) {
        return new Configuration(args);
    }

    public String getSourceOutputDirectory() {
        return this.output + "/spooned";
    }

    public String getBinaryOutputDirectory() {
        return this.output + "/spooned-classes/";
    }

    private static JSAP initJSAP() {
        JSAP jsap = new JSAP();

        FlaggedOption classpath = new FlaggedOption("classpath");
        classpath.setLongFlag("classpath");
        classpath.setShortFlag('c');
        classpath.setHelp("[Mandatory] Use must specify a complete classpath to execute tests on your project."
                + Util.LINE_SEPARATOR +
                "The classpath should be formatted according to the OS.");
        classpath.setStringParser(JSAP.STRING_PARSER);
        classpath.setAllowMultipleDeclarations(false);
        classpath.setRequired(true);

        FlaggedOption testClass = new FlaggedOption("testClass");
        testClass.setLongFlag("test-class");
        testClass.setShortFlag('t');
        testClass.setHelp("[Mandatory] Use must specify a failing test class."
                + Util.LINE_SEPARATOR +
                "You must provide a full qualified name such as: my.package.example.ClassTest"
        );
        testClass.setStringParser(JSAP.STRING_PARSER);
        testClass.setAllowMultipleDeclarations(false);
        testClass.setRequired(true);

        FlaggedOption testMethod = new FlaggedOption("testMethod");
        testMethod.setLongFlag("test-method");
        testMethod.setShortFlag('m');
        testMethod.setHelp("[Mandatory] Use must specify at least one failing test method."
                + Util.LINE_SEPARATOR +
                "Separate multiple values with \":\" such as: test1:test2");
        testMethod.setStringParser(JSAP.STRING_PARSER);
        testMethod.setAllowMultipleDeclarations(false);
        testMethod.setRequired(true);

        FlaggedOption sourcePath = new FlaggedOption("sourcePath");
        sourcePath.setLongFlag("source-path");
        sourcePath.setShortFlag('s');
        sourcePath.setHelp("[Optional] Specify the path to the source folder."
                + Util.LINE_SEPARATOR +
                "(default: src/main/java/)");
        sourcePath.setStringParser(JSAP.STRING_PARSER);
        sourcePath.setAllowMultipleDeclarations(false);
        sourcePath.setDefault("src/main/java/");

        FlaggedOption testPath = new FlaggedOption("testPath");
        testPath.setLongFlag("test-path");
        testPath.setShortFlag('p');
        testPath.setHelp("[Optional] Specify the path to the test source folder."
                + Util.LINE_SEPARATOR +
                " (default: src/test/java/)");
        testPath.setStringParser(JSAP.STRING_PARSER);
        testPath.setAllowMultipleDeclarations(false);
        testPath.setDefault("src/main/java/");

        Switch verbose = new Switch("verbose");
        verbose.setLongFlag("verbose");
        verbose.setHelp("Enable verbose mode");
        verbose.setDefault("false");

        FlaggedOption output = new FlaggedOption("output");
        output.setLongFlag("output");
        output.setShortFlag('o');
        output.setHelp("[Optional] Specify an output folder for result and temporary files"
                + Util.LINE_SEPARATOR +
                "(default: target/assert-fixer)");
        output.setDefault("target/assert-fixer");
        output.setStringParser(JSAP.STRING_PARSER);
        output.setAllowMultipleDeclarations(false);

        Switch help = new Switch("help");
        help.setLongFlag("help");
        help.setShortFlag('h');
        help.setHelp("Display this help and exit");
        help.setDefault("false");


        try {
            jsap.registerParameter(classpath);
            jsap.registerParameter(testClass);
            jsap.registerParameter(testMethod);
            jsap.registerParameter(sourcePath);
            jsap.registerParameter(testPath);
            jsap.registerParameter(verbose);
            jsap.registerParameter(output);
            jsap.registerParameter(help);
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            usage();
        }

        return jsap;
    }

    private static void usage() {
        System.err.println("                          " + jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(1);
    }
}
