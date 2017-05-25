package fr.inria.spirals.run;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

import static fr.inria.spirals.util.Util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class TaskBuilder {

    public static void buildTaskFor(File jsonFile) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(jsonFile));
            final String projectBugID = JSON_FILE_TO_STRING_WITHOUT_EXTENSION.apply(jsonFile);
            final String bugID = extractDigitAtEndOfString(projectBugID);
            final String project = projectBugID.substring(0, projectBugID.length() - bugID.toString().length());
            jsonObject.keySet().stream().filter(o -> ((String) o).equals("seed166")).forEach(key -> {
            //jsonbject.keySet().forEach(key -> {
                final String valueBugExposingTest = (String) ((JSONObject) jsonObject.get(key)).get(BUG_EXPOSING_TEST_KEY);
                if (!EMPTY_ARRAY_AS_STRING.equals(valueBugExposingTest)) {
                    final String seedID = extractDigitAtEndOfString((String) key);
                    try {
                        String fullQualifiedName = findFullQualifiedName(project, bugID, seedID);
                        fullQualifiedName = fullQualifiedName.substring(0, fullQualifiedName.length() - EXTENSION_JAVA.length());
                        Run.runAllTestCaseName(project, bugID, seedID,
                                STRING_ARRAY_TO_INDEX.apply(valueBugExposingTest),
                                fullQualifiedName);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String findFullQualifiedName(String project, String bugID, String seed) {
        File current = new File(PATH_TO_TEST4REPAIR_RESULTS + project + "/" + bugID + "/" + seed);
        if (current.listFiles() == null) {
            throw new RuntimeException(PATH_TO_TEST4REPAIR_RESULTS + project + "/" + bugID + "/" + seed + " not found or empty");
        } else {
            current = current.listFiles()[0];
            StringBuilder fullQualifiedName = new StringBuilder();
            while (!(CONTAINS_JAVA_FILE.test(current))) {
                fullQualifiedName.append(current.getName()).append(".");
                current = current.listFiles()[0];
            }
            fullQualifiedName.append(current.getName()).append(".");
            fullQualifiedName.append(GET_ESTEST.apply(current.listFiles()));
            return fullQualifiedName.toString();
        }
    }



}
