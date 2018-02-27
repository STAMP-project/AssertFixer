package eu.stamp.asserts;

import eu.stamp.test.TestRunner;
import eu.stamp.asserts.log.Logger;
import eu.stamp.util.Counter;
import org.junit.runner.notification.Failure;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.stamp.asserts.AssertionReplacer.isAssert;
import static eu.stamp.asserts.AssertionReplacer.isAssertionClass;

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
                return isAssert.test(element);
            }
        }).size());

        final Throwable exception = failure.getException();
        if (exception instanceof java.lang.AssertionError) {
            Counter.incNumberOfFailingAssertion();
            Counter.incNumberOfFailingTestFromAssertion();
            CtMethod<?> clone = testCaseToBeFix.clone();
            classTestToBeFixed.removeMethod(testCaseToBeFix);
            classTestToBeFixed.addMethod(clone);
            if ((exception.getMessage() != null && exception.getMessage().startsWith(PREFIX_MESSAGE_EXPECTED_EXCEPTION)
                    && exception.getMessage().endsWith("Exception"))) {
                removeExpectedException(spoon, fullQualifiedName, testCaseName, cp, clone);//TODO this remove the fail failure but there is no more oracle
            } else if (exception.getMessage() != null && !exception.getMessage().contains("expected")) {
                return;
            } else {
                // replace assertion
                final List<Integer> indexToLog = AssertionReplacer.replace(clone);
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
            Counter.incNumberOfFailingTestFromException();
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

    private static void removeExpectedException(Launcher spoon, String fullQualifiedName, String testCaseName, String cp, CtMethod<?> clone) {
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
        Class<?> exceptionClass = exception.getClass();
        String exceptionName = exception.getClass().getSimpleName();
        if (exception.getClass().isAnonymousClass()) {
            exceptionClass = exceptionClass.getSuperclass();
            exceptionName = exceptionClass.getSimpleName();
        }
        final CtCatchVariable catchVariable = testCaseToBeFix.getFactory().createCatchVariable(
                testCaseToBeFix.getFactory().Type().createReference(exceptionClass),
                PREFIX_NAME_EXPECTED_EXCEPTION + exceptionName
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
    private static void fixAssertion(Factory factory, CtMethod<?> testCaseToBeFix, List<Integer> indices) {
        indices.forEach(index -> {
                    boolean replaced = false;
                    final CtElement valueToReplace = (CtElement) ((CtInvocation) testCaseToBeFix.getBody()
                            .getStatement(index)).getArguments().get(0);
                    final CtComment comment = factory.createComment("AssertFixer: old assertion " + testCaseToBeFix.getBody().getStatement(index).toString(),
                            CtComment.CommentType.INLINE);
                    if (Logger.observations.containsKey(index)) {
                        if (Logger.observations.get(index) != null &&
                                Logger.observations.get(index).getClass().isArray()) {
                            if (isPrimitiveArray.test(Logger.observations.get(index))) {//TODO only primitive are supported
                                String snippet = createSnippetFromObservations(Logger.observations.get(index));
                                valueToReplace.replace(factory.createCodeSnippetExpression(snippet));
                                Counter.incNumberOfArrayFixed();
                                replaced = true;
                            }
                        } else if (!((valueToReplace instanceof CtLiteral) && Logger.observations.get(index).equals(((CtLiteral) valueToReplace).getValue()))) {
                            if (Logger.observations.get(index) instanceof Boolean) {
                                String snippet = ((CtInvocation) testCaseToBeFix.getBody().getStatement(index)).getTarget().toString() +
                                        ".assert" + Logger.observations.get(index).toString().toUpperCase().substring(0, 1) + Logger.observations.get(index).toString().substring(1)
                                        + "(" + valueToReplace + ")";
                                testCaseToBeFix.getBody().getStatement(index).replace(factory.createCodeSnippetStatement(snippet));
                            } else if ("assertSame".equals(((CtInvocation) valueToReplace.getParent()).getExecutable().getSimpleName())) {
                                ((CtInvocation) valueToReplace.getParent()).replace(factory.createCodeSnippetStatement(
                                        valueToReplace.getParent().toString().replace("assertSame", "assertNotSame")
                                        )
                                );
                            } else if ("assertNotSame".equals(((CtInvocation) valueToReplace.getParent()).getExecutable().getSimpleName())) {
                                ((CtInvocation) valueToReplace.getParent()).replace(factory.createCodeSnippetStatement(
                                        valueToReplace.getParent().toString().replace("assertSame", "assertSame")
                                        )
                                );
                            } else if (isFieldOfClass.test(Logger.observations.get(index))) {
                                valueToReplace.replace(
                                        factory.createCodeSnippetExpression(
                                                fieldOfObjectToString.apply(Logger.observations.get(index))
                                        )
                                );
                            } else if (Logger.observations.get(index).equals(Double.NEGATIVE_INFINITY)) {
                                final CtFieldRead<Double> fieldNegativeInfinity = factory.createFieldRead();
                                fieldNegativeInfinity.setType(factory.createCtTypeReference(Double.class));
                                final CtField<Double> negative_infinity = (CtField<Double>) factory.Class().get(Double.class).getField("NEGATIVE_INFINITY");
                                fieldNegativeInfinity.setVariable(negative_infinity.getReference());
                                fieldNegativeInfinity.setFactory(factory);
                                valueToReplace.replace(fieldNegativeInfinity);
                            } else if (Logger.observations.get(index).equals(Double.POSITIVE_INFINITY)) {
                                final CtFieldRead<Double> fieldPositiveInfinity = factory.createFieldRead();
                                fieldPositiveInfinity.setType(factory.createCtTypeReference(Double.class));
                                final CtField<Double> positive_infinity = (CtField<Double>) factory.Class().get(Double.class).getField("POSITIVE_INFINITY");
                                fieldPositiveInfinity.setVariable(positive_infinity.getReference());
                                fieldPositiveInfinity.setFactory(factory);
                                valueToReplace.replace(fieldPositiveInfinity);
                            } else {
                                valueToReplace.replace(
                                        factory.createLiteral(
                                                Logger.observations.get(index)
                                        )
                                );
                                Counter.incNumberOfPrimitivesValuesFixed();
                            }
                            replaced = true;
                        }
                        if (replaced) {
                            testCaseToBeFix.getBody().getStatement(index).addComment(comment);
                        }
                    }
                }
        );
        Logger.reset();
    }

    private static final Function<Object, String> fieldOfObjectToString = (object) ->
            object.getClass().getSimpleName() + "." + object.toString();

    private static final Predicate<Object> isFieldOfClass = (object) -> {
        try {
            return null != object.getClass().getField(object.toString());
        } catch (NoSuchFieldException e) {
            return false;
        }
    };

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

    @SuppressWarnings("unchecked")
    private static void fixTryCatchFailAssertion(Launcher spoon, CtMethod<?> testCaseToBeFix, Throwable exception, List<CtCatch> catches) {
        Class exceptionClass = exception.getClass();
        String exceptionName = exception.getClass().getSimpleName();
        if (exception.getClass().isAnonymousClass()) {
            exceptionClass = exceptionClass.getSuperclass();
            exceptionName = exceptionClass.getSimpleName();
        }
        final CtCatchVariable<? extends Throwable> catchVariable = testCaseToBeFix.getFactory().createCatchVariable(
                testCaseToBeFix.getFactory().Type().createReference(exceptionClass),
                PREFIX_NAME_EXPECTED_EXCEPTION + exceptionName
        );
        if (!catches.get(0).getBody().getStatements().isEmpty()) {
            catches.get(0).getBody().getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class) {
                @Override
                public boolean matches(CtVariableAccess element) {
                    return element.getVariable().getDeclaration().equals(catches.get(0).getParameter()) && super.matches(element);
                }
            }).forEach(variableAccess -> variableAccess.replace(spoon.getFactory().createVariableRead(catchVariable.getReference(), false)));
        }
        catches.get(0).setParameter(catchVariable);


        // update fail() statement
        final List<CtInvocation> failInvocation = testCaseToBeFix.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return element.getExecutable() != null &&
                        element.getExecutable().getDeclaringType() != null &&
                        "Assert".equals(element.getExecutable().getDeclaringType().getSimpleName()) &&
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
