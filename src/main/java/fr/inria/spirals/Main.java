package fr.inria.spirals;

import fr.inria.spirals.run.TaskBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.spirals.util.Util.IS_JSON_FILE;
import static fr.inria.spirals.util.Util.PATH_TO_TEST4REPAIR_RESULTS;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/05/17
 */
public class Main {

    // TODO UNFIX MATH 33
    // TODO FIX MATH 80

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException {
        final File directoryOfResults = new File(PATH_TO_TEST4REPAIR_RESULTS);
        if (directoryOfResults.listFiles() == null) {
            //TODO should clone repos test4repair-experiments
        } else {
            final List<File> collect = Arrays.stream(directoryOfResults.listFiles())
                    .filter(IS_JSON_FILE)
                    .collect(Collectors.toList());
            collect.stream().filter(file -> file.getName().startsWith("chart17")).forEach(
            //collect.stream().forEach(
                    TaskBuilder::buildTaskFor
            );
        }
    }
}
