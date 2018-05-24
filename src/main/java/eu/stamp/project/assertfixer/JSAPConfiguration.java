package eu.stamp.project.assertfixer;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import eu.stamp.project.assertfixer.util.Util;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

public class JSAPConfiguration extends Configuration {
    private static final String SYSTEM_SEPARATOR = File.pathSeparator;
    private static final JSAP jsap = initJSAP();

    private JSAPConfiguration(String[] args) {
        final JSAPResult options = jsap.parse(args);
        if (options.getBoolean("help") || !options.success()) {
            if (!options.getBoolean("help")) {
                for (java.util.Iterator<?> errs = options.getErrorMessageIterator(); errs.hasNext();) {
                    System.err.println("Error: " + errs.next());
                }
            }
            usage();
        }
        this.setClasspath(options.getString("classpath"));
        this.setFullQualifiedFailingTestClass(options.getString("testClass"));
        this.setFailingTestMethods(Arrays.asList(options.getString("testMethod").split(SYSTEM_SEPARATOR)));
        this.setPathToSourceFolder(Arrays.asList(options.getString("sourcePath").split(SYSTEM_SEPARATOR)));
        this.setPathToTestFolder(Arrays.asList(options.getString("testPath").split(SYSTEM_SEPARATOR)));
        this.setVerbose(options.getBoolean("verbose"));
        this.setOutput(options.getString("output"));
        this.setGenTryCatch(options.getBoolean("genTryCatch"));
    }

    public static JSAPConfiguration get(String[] args) {
        return new JSAPConfiguration(args);
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
                "Separate multiple values with \""+ SYSTEM_SEPARATOR +"\" such as: test1"+SYSTEM_SEPARATOR+"test2");
        testMethod.setStringParser(JSAP.STRING_PARSER);
        testMethod.setAllowMultipleDeclarations(false);
        testMethod.setRequired(true);

        FlaggedOption sourcePath = new FlaggedOption("sourcePath");
        sourcePath.setLongFlag("source-path");
        sourcePath.setShortFlag('s');
        sourcePath.setHelp("[Optional] Specify the path to the source folder."
                + Util.LINE_SEPARATOR +
                "Separate multiple values with \""+ SYSTEM_SEPARATOR +"\" such as: path/one"+SYSTEM_SEPARATOR+"path/two/");
        sourcePath.setStringParser(JSAP.STRING_PARSER);
        sourcePath.setAllowMultipleDeclarations(false);
        sourcePath.setRequired(false);

        FlaggedOption testPath = new FlaggedOption("testPath");
        testPath.setLongFlag("test-path");
        testPath.setShortFlag('p');
        testPath.setHelp("Specify the path to the test source folder."
                + Util.LINE_SEPARATOR +
                "Separate multiple values with \""+ SYSTEM_SEPARATOR +"\" such as: path/one"+SYSTEM_SEPARATOR+"path/two/");
        testPath.setStringParser(JSAP.STRING_PARSER);
        testPath.setAllowMultipleDeclarations(false);
        testPath.setDefault("src/test/java/");

        Switch verbose = new Switch("verbose");
        verbose.setLongFlag("verbose");
        verbose.setHelp("Enable verbose mode");
        verbose.setDefault("false");

        FlaggedOption output = new FlaggedOption("output");
        output.setLongFlag("output");
        output.setShortFlag('o');
        output.setHelp("[Optional] Specify an output folder for result and temporary files.");
        output.setDefault("target/assert-fixer");
        output.setStringParser(JSAP.STRING_PARSER);
        output.setAllowMultipleDeclarations(false);

        Switch genTryCatch = new Switch("genTryCatch");
        genTryCatch.setLongFlag("gen-try-catch");
        genTryCatch.setDefault("false");
        genTryCatch.setHelp("Enable the generation of try/catch/fail block to repair test method");

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
            jsap.registerParameter(genTryCatch);
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
