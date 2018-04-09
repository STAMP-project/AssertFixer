package eu.stamp.project.assertfixer.asserts;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import eu.stamp.project.assertfixer.AssertFixerResult;
import eu.stamp.project.assertfixer.Configuration;
import eu.stamp.project.assertfixer.asserts.log.Logger;
import eu.stamp.project.assertfixer.test.TestRunner;
import eu.stamp.project.assertfixer.util.Counter;
import eu.stamp.project.assertfixer.util.Util;
import eu.stamp.project.testrunner.runner.test.Failure;
import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/05/17
 */
public class AssertFixer {

    public static void fixAssert(Configuration configuration, Launcher spoon, AssertFixerResult fixerResult, Failure failure, String cp) throws MalformedURLException, ClassNotFoundException {
        final CtClass<?> classTestToBeFixed = spoon.getFactory().Class().get(fixerResult.getTestClass());
        CtMethod<?> testCaseToBeFix = classTestToBeFixed.getMethodsByName(fixerResult.getTestMethod()).get(0);
        String oldMethodString = testCaseToBeFix.toString();

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
                removeExpectedException(configuration, spoon, fixerResult.getTestClass(), fixerResult.getTestMethod(), cp, clone);//TODO this remove the fail failure but there is no more oracle
            } else if (failure.messageOfFailure != null && !failure.messageOfFailure.contains("expected")) {
                return;
            } else {
                // replace assertion
                final List<Integer> indexToLog = AssertionReplacer.replaceByLogStatement(clone);
                // run tests
                TestRunner.runTestWithLogger(configuration, spoon, cp, fixerResult.getTestClass(), fixerResult.getTestMethod());
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

        computeDiff(fixerResult, oldMethodString, testCaseToBeFix.toString());
    }

    private static void computeDiff(AssertFixerResult fixerResult, String oldMethod, String newMethod) {
        List<String> oldMethodLines = Arrays.asList(oldMethod.split("\\n"));
        List<String> newMethodLines = Arrays.asList(newMethod.split("\\n"));

        try {
            Patch<String> patch = DiffUtils.diff(oldMethodLines, newMethodLines);
            fixerResult.setPatch(patch);

            DiffRowGenerator rowGenerator = DiffRowGenerator.create()
                                            .showInlineDiffs(false)
                                            .mergeOriginalRevised(false)
                                            .reportLinesUnchanged(true)
                                            .build();

            List<DiffRow> diffRows = rowGenerator.generateDiffRows(oldMethodLines, newMethodLines);

            StringBuilder stringBuilder = new StringBuilder();
            for (DiffRow diffRow : diffRows) {
                if (!diffRow.toString().isEmpty()) {
                    switch (diffRow.getTag()) {
                        case DELETE:
                            stringBuilder.append("- ");
                            stringBuilder.append(diffRow.getOldLine());
                            break;

                        case INSERT:
                            stringBuilder.append("+ ");
                            stringBuilder.append(diffRow.getNewLine());
                            break;

                        case CHANGE:
                            stringBuilder.append("- ");
                            stringBuilder.append(diffRow.getOldLine());
                            stringBuilder.append("+ ");
                            stringBuilder.append(diffRow.getNewLine());
                            break;

                        case EQUAL:
                            stringBuilder.append(diffRow.getNewLine());
                    }
                    stringBuilder.append('\n');
                }
            }
            fixerResult.setDiff(stringBuilder.toString());
        } catch (DiffException e) {
            e.printStackTrace();
        }
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
