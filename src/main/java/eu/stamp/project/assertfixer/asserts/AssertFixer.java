package eu.stamp.project.assertfixer.asserts;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import eu.stamp.project.assertfixer.AssertFixerResult;
import eu.stamp.project.assertfixer.Configuration;
import eu.stamp.project.assertfixer.asserts.log.Logger;
import eu.stamp.project.assertfixer.test.TestRunner;
import eu.stamp.project.assertfixer.util.Counter;
import eu.stamp.project.assertfixer.util.Util;
import eu.stamp.project.testrunner.EntryPoint;
import eu.stamp.project.testrunner.runner.test.Failure;
import org.apache.commons.lang3.StringUtils;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/05/17
 */
public class AssertFixer {

    public static AssertFixerResult fixAssert(Configuration configuration, Launcher spoon, CtClass originalClass, String testCaseName, Failure failure, String cp) throws MalformedURLException, ClassNotFoundException {
        final CtClass<?> classTestToBeFixed = originalClass.clone();
        final String originalClassStr = originalClass.toString();
        final String filePath = originalClass.getPosition().getFile().getPath();
        final String basePath = new File(".").getPath();
        String relativeFilePath = new File(basePath).toURI().relativize(new File(filePath).toURI()).getPath();
        final CtPackage parentPackage = originalClass.getPackage();

        // switch the original class and the clone from the model
        parentPackage.removeType(originalClass);
        parentPackage.addType(classTestToBeFixed);

        String testClassName = originalClass.getQualifiedName();

        AssertFixerResult result = new AssertFixerResult(testClassName, testCaseName);
        CtMethod<?> testCaseToBeFix = classTestToBeFixed.getMethodsByName(testCaseName).get(0);

        Counter.addNumberOfAssertionInTests(testCaseToBeFix.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return Util.isAssert.test(element);
            }
        }).size());


        if (Util.assertionErrors.contains(failure.fullQualifiedNameOfException)) {
            Counter.incNumberOfFailingAssertion();
            Counter.incNumberOfFailingTestFromAssertion();
            CtMethod<?> clone = testCaseToBeFix.clone();
            classTestToBeFixed.removeMethod(testCaseToBeFix);
            classTestToBeFixed.addMethod(clone);
            if ((failure.messageOfFailure != null &&
                    failure.messageOfFailure.startsWith(TryCatchFixer.PREFIX_MESSAGE_EXPECTED_EXCEPTION)
                    && failure.messageOfFailure.endsWith("Exception"))) {
                removeExpectedException(configuration, spoon, testClassName, testCaseName, cp, clone);//TODO this remove the fail failure but there is no more oracle
            } else if (failure.messageOfFailure != null && !failure.messageOfFailure.contains("expected")) {
                String message = "AssertFixer cannot fix this assertion. Message of failure: "+failure.messageOfFailure;
                result.setSuccess(false);
                result.setExceptionMessage(message);
            } else {
                // replace assertion
                final List<Integer> indexToLog = AssertionReplacer.replaceByLogStatement(clone);
                // run tests
                TestRunner.runTestWithLogger(configuration, spoon, cp, testClassName, testCaseName);
                Logger.load();
                // replace wrong value
                classTestToBeFixed.removeMethod(clone);
                classTestToBeFixed.addMethod(testCaseToBeFix);
                AssertionsFixer.fixAssertion(spoon.getFactory(), testCaseToBeFix, indexToLog);
            }
        } else {
            Counter.incNumberOfFailingTestFromException();
            final List<CtCatch> catches = testCaseToBeFix.getElements(new TypeFilter<>(CtCatch.class));
            if (!catches.isEmpty()) {
                TryCatchFixer.fixTryCatchFailAssertion(spoon, testCaseToBeFix, failure, catches);
            } else {
                TryCatchFixer.addTryCatchFailAssertion(spoon, testCaseToBeFix, failure);
            }
        }


        SpoonModelBuilder compiler = spoon.createCompiler();
        compiler.setBinaryOutputDirectory(new File(configuration.getBinaryOutputDirectory()));
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);

        boolean success = EntryPoint.runTests(
                configuration.getBinaryOutputDirectory() +
                        Util.PATH_SEPARATOR + configuration.getClasspath(),
                testClassName,
                testCaseName
        ).getFailingTests().isEmpty();

        String diff = computeDiff(originalClassStr, classTestToBeFixed.toString(), relativeFilePath);
        result.setDiff(diff);
        result.setSuccess(success);

        // switch back the clone and the original class in the model for other changes
        parentPackage.removeType(classTestToBeFixed);
        parentPackage.addType(originalClass);
        return result;
    }

    private static String computeDiff(String oldClassStr, String newClassStr, String filePath) {
        List<String> oldClassLines = Arrays.asList(oldClassStr.split("\\n"));
        List<String> newClassLines = Arrays.asList(newClassStr.split("\\n"));

        try {
            Patch<String> patch = DiffUtils.diff(oldClassLines, newClassLines);
            List<String> strings = UnifiedDiffUtils.generateUnifiedDiff(filePath, filePath, oldClassLines, patch, 0);

            return StringUtils.join(strings, "\n");
        } catch (DiffException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void removeExpectedException(Configuration configuration, Launcher spoon, String fullQualifiedName, String testCaseName, String cp, CtMethod<?> clone) {
        final CtTry ctTry = clone.getElements(new TypeFilter<>(CtTry.class)).get(0);
        ctTry.replace(ctTry.getBody());
        final CtInvocation failToRemove = clone.getElements(new TypeFilter<>(CtInvocation.class)).stream()
                .filter(Util.isFail)
                .findFirst()
                .orElseThrow(RuntimeException::new);
        final boolean remove = failToRemove.getParent(CtBlock.class).getStatements().remove(failToRemove);
        if (!remove) {
            throw new RuntimeException();
        }
        TestRunner.runTest(configuration, spoon, fullQualifiedName, testCaseName);
    }

}
