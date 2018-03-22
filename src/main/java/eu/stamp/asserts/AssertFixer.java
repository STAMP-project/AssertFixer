package eu.stamp.asserts;

import eu.stamp.EntryPoint;
import eu.stamp.Main;
import eu.stamp.asserts.log.Logger;
import eu.stamp.runner.test.Failure;
import eu.stamp.util.Counter;
import eu.stamp.util.Util;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/05/17
 */
public class AssertFixer {

    public static void fixAssert(Launcher spoon, String fullQualifiedName, String testCaseName, Failure failure, String cp) throws MalformedURLException, ClassNotFoundException {
        final CtClass<?> classTestToBeFixed = spoon.getFactory().Class().get(fullQualifiedName);
        System.out.println(fullQualifiedName + "#" + testCaseName);
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
                removeExpectedException(spoon, fullQualifiedName, testCaseName, cp, clone);//TODO this remove the fail failure but there is no more oracle
            } else if (failure.messageOfFailure != null && !failure.messageOfFailure.contains("expected")) {
                return;
            } else {
                // replace assertion
                final List<Integer> indexToLog = AssertionReplacer.replaceByLogStatement(clone);
                // run tests
                eu.stamp.test.TestRunner.runTestWithLogger(spoon, cp, fullQualifiedName, testCaseName);
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
    }

    private static void removeExpectedException(Launcher spoon, String fullQualifiedName, String testCaseName, String cp, CtMethod<?> clone) {
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
        final SpoonModelBuilder compiler = spoon.createCompiler();
        compiler.setBinaryOutputDirectory(new File(Main.configuration.getBinaryOutputDirectory()));
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        try {
            EntryPoint.runTests(cp, fullQualifiedName, testCaseName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
