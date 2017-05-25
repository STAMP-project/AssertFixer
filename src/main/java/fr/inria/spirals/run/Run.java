package fr.inria.spirals.run;

import fr.inria.spirals.asserts.log.Logger;
import fr.inria.spirals.test.TestRunner;
import org.junit.runner.notification.Failure;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

import java.util.List;

import static fr.inria.spirals.asserts.AssertFixer.fixAssert;
import static fr.inria.spirals.util.Util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class Run {

    public static void runAllTestCaseName(String project, String bugId, String seed, String [] testCaseName, String fullQualifiedName) throws Throwable {
        Launcher spoon = buildSpoonModel(project, bugId, seed);
        for (String s : testCaseName) {
            run(spoon, project, bugId, seed, s, fullQualifiedName);
        }
        String cp = getBaseClassPath(project, bugId);
        cp += PATH_SEPARATOR + spoon.getEnvironment().getBinaryOutputDirectory();
        final SpoonModelBuilder compiler = spoon.createCompiler();
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        if (TestRunner.runTest(fullQualifiedName, cp.split(":")).isEmpty()) {
            System.out.println("project:" + project);
            System.out.println("bugId:" + bugId);
            System.out.println("seed:" + seed);
            System.out.println("Fixing Assert succeed!");
            spoon.getFactory().Class().get(Logger.class).delete();
            spoon.getFactory().Class().get(Logger.class).updateAllParentsBelow();
            spoon.prettyprint();
        } else {
            System.out.println("project:" + project);
            System.out.println("bugId:" + bugId);
            System.out.println("seed:" + seed);
            System.exit(-1);
        }
    }

    public static void run(Launcher spoon, String project, String bugId, String seed, String testCaseName, String fullQualifiedName) throws Throwable {
        testCaseName = spoon.getFactory().Class().get(fullQualifiedName).getMethodsByName("test" + testCaseName).isEmpty() ?
                "test0" + testCaseName : "test" + testCaseName;

        String cp = getBaseClassPath(project, bugId);
        cp += PATH_SEPARATOR + spoon.getEnvironment().getBinaryOutputDirectory();
        final List<Failure> failures = TestRunner.runTest(
                fullQualifiedName,
                testCaseName,
                cp.split(":"));// should fail bug exposing test

        System.out.println(project + " >> " + bugId + " >> " + seed);

        fixAssert(spoon,
                fullQualifiedName,
                testCaseName,
                failures.get(0),
                cp);
    }


    private static Launcher buildSpoonModel(String project, String bugId, String seed) {
        Launcher spoon = new Launcher();
        spoon.addInputResource(PATH_TO_TEST4REPAIR_RESULTS + project + FILE_SEPARATOR + bugId + FILE_SEPARATOR + seed);
        spoon.addInputResource("src/main/java/fr/inria/spirals/asserts/log/Logger.java"); // adding the logger to the spoon model to compile and run it to fix assertions
        spoon.getEnvironment().setComplianceLevel(7);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.setSourceOutputDirectory("output" + FILE_SEPARATOR + project + FILE_SEPARATOR + bugId + FILE_SEPARATOR + seed);
        String cp = getBaseClassPath(project, bugId);
        spoon.getEnvironment().setSourceClasspath((cp).split(PATH_SEPARATOR));
        spoon.run();
        return spoon;
    }
}
