package fr.inria.spirals.test4repair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.spirals.json.JSONProject;
import fr.inria.spirals.util.Counter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.inria.spirals.util.Util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/05/17
 */
public class Main {

    public static Map<String, JSONProject> projects = new HashMap<>();

    public static final String[] projectsName = new String[]{"chart", "lang", "math", "time"};

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException {
        final File directoryOfResults = new File(UtilTest4Repair.PATH_TO_TEST4REPAIR_RESULTS);
        if (directoryOfResults.listFiles() == null) {
            //TODO should clone repos test4repair-experiments
        } else {
            final List<File> collect = Arrays.stream(directoryOfResults.listFiles())
                    .filter(IS_JSON_FILE)
                    .collect(Collectors.toList());
//            collect.stream().filter(file -> file.getName().startsWith("chart5")).forEach(
            collect.forEach(
                    TaskBuilder::buildTaskFor
            );
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Arrays.stream(projectsName).forEach(name -> {
                        try (FileWriter writer = new FileWriter(new File("output" + FILE_SEPARATOR + name + EXTENSION_JSON), false)) {
                            writer.write(gson.toJson(projects.get(name)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            Counter.print();
        }
    }
}
