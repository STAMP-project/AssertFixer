package eu.stamp;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Configuration {

    public final String output;

    public Configuration() {
        this(new String[0]);
    }

    public Configuration(String [] args) {
        final JSAPResult options = initJSAP().parse(args);
        this.output = options.getString("output");
    }

    public String getSourceOutputDirectory() {
        return this.output + "/spooned";
    }

    public String getBinaryOutputDirectory() {
        return this.output + "/spooned-classes/";
    }

    private static JSAP initJSAP() {
        JSAP jsap = new JSAP();

        FlaggedOption output = new FlaggedOption("output");
        output.setLongFlag("output");
        output.setShortFlag('o');
        output.setHelp("Specify an output folder for result and temporary files (default: target/assert-fixer)");
        output.setDefault("target/assert-fixer");
        output.setStringParser(JSAP.STRING_PARSER);
        output.setAllowMultipleDeclarations(false);


        try {
            jsap.registerParameter(output);
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            usage();
        }

        return jsap;
    }

    private static void usage() {

    }

}
