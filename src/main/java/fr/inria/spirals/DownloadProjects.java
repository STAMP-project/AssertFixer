package fr.inria.spirals;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.inria.spirals.util.Util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class DownloadProjects {

    public static void main(String[] args) {
        final File directoryOfResults = new File(PATH_TO_TEST4REPAIR_RESULTS);
        if (directoryOfResults.listFiles() == null) {
            //TODO should clone repos test4repair-experiments
        } else {
            final List<File> collect = Arrays.stream(directoryOfResults.listFiles())
                    .filter(IS_JSON_FILE)
                    .collect(Collectors.toList());
            collect.forEach(
                    DownloadProjects::initProjectFromJSON
            );
        }
    }

    private static void initProjectFromJSON(File jsonFile) {
        final String projectBugID = JSON_FILE_TO_STRING_WITHOUT_EXTENSION.apply(jsonFile);
        final String bugID = extractDigitAtEndOfString(projectBugID);
        final String project = projectBugID.substring(0, projectBugID.length() - bugID.toString().length());
        initProjects(project, bugID);
        String cmd = "defects4j checkout -p " +
                putFirstLetterToUpperCase.apply(project) +
                " -v " +
                bugID + "f" +
                " -w " +
                project + "_" + bugID + " && " +
                "cd " + project + "_" + bugID + " && " +
                "defects4j compile && cd ..";
        System.out.println(cmd);
        cmd = "defects4j checkout -p " +
                putFirstLetterToUpperCase.apply(project) +
                " -v " +
                bugID + "b" +
                " -w " +
                project + "_" + bugID + "b" + " && " +
                "cd " + project + "_" + bugID + "b && " +
                "defects4j compile && cd ..";
        System.out.println(cmd);
    }

    private static void initProjects(String project, String bugID) {
        String cmd = "defects4j checkout -p " +
                putFirstLetterToUpperCase.apply(project) +
                " -v " +
                bugID + "f" +
                " -w " +
                project + "_" + bugID + " && " +
                "cd " + project + "_" + bugID + " && " +
                "defects4j compile && cd ..";
        System.out.println(cmd);
        cmd = "defects4j checkout -p " +
                putFirstLetterToUpperCase.apply(project) +
                " -v " +
                bugID + "b" +
                " -w " +
                project + "_" + bugID + "b" + " && " +
                "cd " + project + "_" + bugID + "b && " +
                "defects4j compile && cd ..";
        System.out.println(cmd);
    }

    private static final Function<String, String> putFirstLetterToUpperCase = string ->
            string.toUpperCase().substring(0, 1) + string.substring(1);
}
