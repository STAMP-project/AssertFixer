package test4repair;

import fr.inria.spirals.util.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/05/17
 */
public class UtilTest4Repair {
    public static final String PATH_TO_TEST4REPAIR = "../test4repair-experiments/";

    public static final String PATH_TO_PROJECTS_JSON = PATH_TO_TEST4REPAIR +
            "src" + Util.FILE_SEPARATOR +
            "Nopol+UnsatGuided" + Util.FILE_SEPARATOR +
            "src" + Util.FILE_SEPARATOR +
            "main" + Util.FILE_SEPARATOR +
            "java" + Util.FILE_SEPARATOR +
            "data" + Util.FILE_SEPARATOR +
            "projects" + Util.FILE_SEPARATOR; // TODO this is not java file, why put them into src/main/java ???

    public static final String PATH_TO_TEST4REPAIR_RESULTS = PATH_TO_TEST4REPAIR + "results/Nopol-Test-Runing-Result/"; // + project + "/" + bugId + "/" + seed

    public static final String RELATIVE_PATH_SRC_BIN_KEY = "binjava";

    public static final String RELATIVE_PATH_TEST_BIN_KEY = "bintest";

    public static final String PATH_TO_PROJECTS_REPOS = ".." + Util.FILE_SEPARATOR + "projects" + Util.FILE_SEPARATOR + "";

    public static final String ES_MODULE_NAME = "evosuite-master";

    public static final String EMPTY_ARRAY_AS_STRING = "[]";

    public static final String BUG_EXPOSING_TEST_KEY = "BugExposingTest";

    public static final String TEST_NAME = "_ESTest" + Util.EXTENSION_JAVA;

    public static final Function<File[], String> GET_ESTEST = files ->
            Arrays.stream(files)
                    .filter(file -> file.getName().endsWith(TEST_NAME))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("TestNotFound"))
                    .getName();

    public static final Function<String, String[]> STRING_ARRAY_TO_INDEX = string ->
            string.split("\\[")[1].split("]")[0].replaceAll(" ", "").split(",");

    public static String getBaseClassPath(String project, String bugId) {
        final String relativePathToClasses = getRelativePathToClasses(project, bugId);
        return relativePathToClasses + Util.PATH_SEPARATOR + getEvosuiteDependenciesPath();
    }

    @SuppressWarnings("unchecked")
    public static String getRelativePathToClasses(String project, String bugId) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(
                    new FileReader(new File(PATH_TO_PROJECTS_JSON + project + Util.EXTENSION_JSON))
            );
            final JSONObject src = (JSONObject) jsonObject.get("src");
            final JSONObject rightVersionSources = (JSONObject) src.get(
                    src.keySet().size() == 1 ?
                            src.keySet().stream().findAny().orElseThrow(() -> new RuntimeException(project + "#" + bugId)) :
                            src.get(bugId) != null ? bugId :
                                    (src.keySet()
                                            .stream()
                                            .sorted()
                                            .filter(key -> Integer.parseInt(bugId) - Integer.parseInt((String) key) <= 0).
                                                    reduce((k1, k2) -> k1)
                                            .orElseThrow(() -> new RuntimeException(project + "#" + bugId))
                                            .toString()
                                    )
            );
            return PATH_TO_PROJECTS_REPOS + project + "_" + bugId +
                    Util.FILE_SEPARATOR + rightVersionSources.get(RELATIVE_PATH_SRC_BIN_KEY).toString()
                    + Util.PATH_SEPARATOR +
                    PATH_TO_PROJECTS_REPOS + project + "_" + bugId +
                    Util.FILE_SEPARATOR + rightVersionSources.get(RELATIVE_PATH_TEST_BIN_KEY).toString();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static String getEvosuiteDependenciesPath() {
        if (!new File("target/.cp").exists()) {
            String cmd = "mvn dependency:build-classpath -Dmdep.outputFile=target/.cp";
            try {
                final Process exec = Runtime.getRuntime().exec(cmd);
                exec.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(new File("target/.cp")))) {
            return Arrays.stream(reader.lines()
                    .findFirst()
                    .orElseThrow(RuntimeException::new)
                    .split(Util.PATH_SEPARATOR))
                    .filter(s -> s.contains(ES_MODULE_NAME))
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String EXTENSION_JSON = ".json";

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
