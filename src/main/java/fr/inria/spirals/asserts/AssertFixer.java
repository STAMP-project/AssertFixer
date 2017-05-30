package fr.inria.spirals.asserts;

import fr.inria.spirals.asserts.log.Logger;
import fr.inria.spirals.test.TestRunner;
import org.junit.Assert;
import org.junit.runner.notification.Failure;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fr.inria.spirals.asserts.AssertionReplacer.isAssertionClass;

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
        final Throwable exception = failure.getException();
        if (exception instanceof java.lang.AssertionError) {
            CtMethod<?> clone = testCaseToBeFix.clone();
            classTestToBeFixed.removeMethod(testCaseToBeFix);
            classTestToBeFixed.addMethod(clone);
            if ((exception.getMessage() != null && exception.getMessage().startsWith(PREFIX_MESSAGE_EXPECTED_EXCEPTION)
                    && exception.getMessage().endsWith("Exception"))) {
                replaceExpectedException(spoon, fullQualifiedName, testCaseName, cp, clone);//TODO this remove the fail failure but there is no more oracle
            } else {
                // replace assertion
                final List<Integer> indexToLog = AssertionReplacer.replaceAssertionByLog(clone, spoon.getFactory());
                // run tests
                final SpoonModelBuilder compiler = spoon.createCompiler();
                compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
                TestRunner.runTest(fullQualifiedName, testCaseName, cp.split(":"));
                TestRunner.runTest(fullQualifiedName, testCaseName, cp.split(":"));
                TestRunner.runTest(fullQualifiedName, testCaseName, cp.split(":"));
                // replace wrong value
                classTestToBeFixed.removeMethod(clone);
                classTestToBeFixed.addMethod(testCaseToBeFix);
                fixAssertion(spoon.getFactory(), testCaseToBeFix, indexToLog);
            }
        } else {
            final List<CtCatch> catches = testCaseToBeFix.getElements(new TypeFilter<>(CtCatch.class));
            if (!catches.isEmpty()) {
                fixTryCatchFailAssertion(spoon, testCaseToBeFix, exception, catches);
            } else {
                addTryCatchFailAssertion(spoon, testCaseToBeFix, exception);
            }
        }
    }

    private static final Predicate<CtInvocation> isFail = ctInvocation ->
            ctInvocation.getExecutable().getSimpleName().startsWith("fail") ||
                    isAssertionClass(ctInvocation.getExecutable().getDeclaringType().getDeclaration());

    private static void replaceExpectedException(Launcher spoon, String fullQualifiedName, String testCaseName, String cp, CtMethod<?> clone) {
        final CtTry ctTry = clone.getElements(new TypeFilter<>(CtTry.class)).get(0);
        ctTry.replace(ctTry.getBody());
        final CtInvocation failToRemove = clone.getElements(new TypeFilter<>(CtInvocation.class)).stream()
                .filter(isFail)
                .findFirst()
                .orElseThrow(RuntimeException::new);
        final boolean remove = failToRemove.getParent(CtBlock.class).getStatements().remove(failToRemove);
        if (!remove) {
            throw new RuntimeException();
        }
        final SpoonModelBuilder compiler = spoon.createCompiler();
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        try {
            TestRunner.runTest(fullQualifiedName, testCaseName, cp.split(":"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addTryCatchFailAssertion(Launcher spoon, CtMethod<?> testCaseToBeFix, Throwable exception) {
        final CtTry aTry = spoon.getFactory().createTry();
        final CtCatch aCatch = spoon.getFactory().createCatch();
        aTry.addCatcher(aCatch);
        final CtCatchVariable catchVariable = testCaseToBeFix.getFactory().createCatchVariable(
                testCaseToBeFix.getFactory().Type().createReference(exception.getClass()),
                PREFIX_NAME_EXPECTED_EXCEPTION + exception.getClass().getSimpleName()
        );
        aCatch.setParameter(catchVariable);
        aCatch.setBody(spoon.getFactory().createCodeSnippetStatement(
                "org.junit.Assert.assertTrue(true)"
        ));
        aTry.setBody(testCaseToBeFix.getBody().getStatement(0));
        for (int i = 1; i < testCaseToBeFix.getBody().getStatements().size(); i++) {
            aTry.getBody().addStatement(testCaseToBeFix.getBody().getStatement(i));
        }
        aTry.getBody().addStatement(spoon.getFactory()
                .createCodeSnippetStatement("org.junit.Assert." + NAME_FAIL_METHOD + "(\"" + PREFIX_MESSAGE_EXPECTED_EXCEPTION + exception.getClass().getSimpleName() + "\")")
        );
        testCaseToBeFix.setBody(aTry);
    }

    @SuppressWarnings("unchecked")
    private static void fixAssertion(Factory factory, CtMethod<?> testCaseToBeFix, List<Integer> indexToLog) {
//        int index = indexToLog.get(0);
        indexToLog.forEach(index -> {
                    final CtElement valueToReplace = (CtElement) ((CtInvocation) testCaseToBeFix.getBody()
                            .getStatement(index)).getArguments().get(0);
                    if (Logger.observations.containsKey(index)) {
                        if (Logger.observations.get(index) != null &&
                                Logger.observations.get(index).getClass().isArray()) {
                            if (isPrimitiveArray.test(Logger.observations.get(index))) {//TODO only primitive are supported
                                String snippet = createSnippetFromObservations(Logger.observations.get(index));
                                valueToReplace.replace(factory.createCodeSnippetExpression(snippet));
                            }
                        } else {
                            valueToReplace.replace(
                                    factory.createLiteral(
                                            Logger.observations.get(index)
                                    )
                            );
                        }
                    }
                }
        );
        Logger.reset();
    }

    //TODO fix me to other primitive type
    private static String createSnippetFromObservations(Object o) {
        String snippet = "new " + o.getClass().getSimpleName() + "{ ";
        if (o instanceof int[]) {
            snippet += Arrays.stream((int[]) o).mapToObj(v -> v).map(Object::toString).collect(Collectors.joining(","));
        } else if (o instanceof double[]) {
            snippet += Arrays.stream((double[]) o).mapToObj(v -> v).map(Object::toString).collect(Collectors.joining(","));
        } else if (o instanceof long[]) {
            snippet += Arrays.stream((long[]) o).mapToObj(v -> v).map(Object::toString).collect(Collectors.joining(","));
        }
        return snippet + "}";
    }

    private static final Predicate<Object> isPrimitiveArray = obj ->
            Character.isLowerCase(obj.getClass().getSimpleName().toCharArray()[0]);

    private static final String PREFIX_NAME_EXPECTED_EXCEPTION = "expectedException__";
    private static final String PREFIX_MESSAGE_EXPECTED_EXCEPTION = "Expecting exception: ";
    private static final String NAME_FAIL_METHOD = "fail";

    private static void fixTryCatchFailAssertion(Launcher spoon, CtMethod<?> testCaseToBeFix, Throwable exception, List<CtCatch> catches) {
        final CtCatchVariable<? extends Throwable> catchVariable = testCaseToBeFix.getFactory().createCatchVariable(
                testCaseToBeFix.getFactory().Type().createReference(exception.getClass()),
                PREFIX_NAME_EXPECTED_EXCEPTION + exception.getClass().getSimpleName()
        );
        catches.get(0).setParameter(catchVariable);
        final List<CtInvocation> failInvocation = testCaseToBeFix.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return element.getExecutable() != null &&
                        element.getExecutable().getDeclaringType() != null&&
                        element.getExecutable().getDeclaringType().getDeclaration() != null &&
                        element.getExecutable().getDeclaringType().getDeclaration().getQualifiedName().equals("org.junit.Assert.class") &&
                        NAME_FAIL_METHOD.equals(element.getExecutable().getSimpleName()) &&
                        super.matches(element);
            }
        });
        if (!failInvocation.isEmpty() &&
                failInvocation.get(0).getParent(CtTry.class)
                        .getCatchers().contains(catches.get(0))
                ) {
            ((CtLiteral<String>) failInvocation.get(0).getArguments().get(0)).replace(
                    spoon.getFactory().createLiteral(PREFIX_MESSAGE_EXPECTED_EXCEPTION + exception.getClass().getSimpleName())
            );
        }
    }


}
