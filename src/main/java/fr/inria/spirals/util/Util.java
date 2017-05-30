package fr.inria.spirals.util;

import fr.inria.spirals.test4repair.UtilTest4Repair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class Util {

    public static final String EXTENSION_JSON = ".json";

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final String EXTENSION_JAVA = ".java";

    public static final Predicate<File> IS_JSON_FILE = file -> (file.getName().endsWith(EXTENSION_JSON));

    public static final Predicate<File> IS_JAVA_FILE = file -> (file.getName().endsWith(EXTENSION_JAVA));

    public static final Predicate<File> CONTAINS_JAVA_FILE = file ->
            file.listFiles() != null &&
                    (Arrays.stream(file.listFiles()).anyMatch(IS_JAVA_FILE));

    public static final Function<File, String> JSON_FILE_TO_STRING_WITHOUT_EXTENSION = file ->
            (file.getName().substring(0, file.getName().length() - EXTENSION_JSON.length()));

    public static String extractDigitAtEndOfString(String string) {
        StringBuilder digits = new StringBuilder();
        int index = string.length() - 1;
        final char[] chars = string.toCharArray();
        while (Character.isDigit(chars[index])) {
            digits.append(chars[index]);
            index--;
        }
        return digits.reverse().toString();
    }

}
