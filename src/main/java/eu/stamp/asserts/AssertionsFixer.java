package eu.stamp.asserts;

import eu.stamp.asserts.log.Logger;
import eu.stamp.util.Counter;
import eu.stamp.util.Util;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/03/18
 */
public class AssertionsFixer {

    @SuppressWarnings("unchecked")
    static void fixAssertion(Factory factory, CtMethod<?> testCaseToBeFix, List<Integer> indices) {
        indices.forEach(index -> {
                    boolean replaced = false;
                    final CtElement valueToReplace = (CtElement) ((CtInvocation) testCaseToBeFix.getBody()
                            .getStatement(index)).getArguments().get(0);
                    final CtComment comment = factory.createComment("AssertFixer: old assertion " + testCaseToBeFix.getBody().getStatement(index).toString(),
                            CtComment.CommentType.INLINE);
                    if (Logger.observations.containsKey(index)) {
                        if (Logger.observations.get(index) != null &&
                                Logger.observations.get(index).getClass().isArray()) {
                            if (Util.isPrimitiveArray.test(Logger.observations.get(index))) {//TODO only primitive are supported
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
                            } else if (Util.isFieldOfClass.test(Logger.observations.get(index))) {
                                valueToReplace.replace(
                                        factory.createCodeSnippetExpression(
                                                Util.fieldOfObjectToString.apply(Logger.observations.get(index))
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

    //TODO fix me to other primitive type
    static String createSnippetFromObservations(Object o) {
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

}
