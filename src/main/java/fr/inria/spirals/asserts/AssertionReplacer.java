package fr.inria.spirals.asserts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class AssertionReplacer {

    public static List<Integer> replaceAssertionByLog(CtMethod<?> clone, Factory factory) {
        AssertionRemoverProcessor processor = new AssertionRemoverProcessor();
        QueueProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(processor);
        pm.process(clone);
        return processor.indexToBeLogged;
    }

    private static final class AssertionRemoverProcessor extends AbstractProcessor<CtInvocation> {

        public final List<Integer> indexToBeLogged = new ArrayList<>();

        @Override
        public boolean isToBeProcessed(CtInvocation candidate) {
            return isAssert.test(candidate) && super.isToBeProcessed(candidate);
        }

        // TODO Maybe we have to fix this... because we assume test as assert(expected, actual)
        @Override
        public void process(CtInvocation element) {
            final int index = element.getParent(CtBlock.class).getStatements().indexOf(element);
            final CtMethod<?> ctMethod = element.getParent(CtMethod.class);
            indexToBeLogged.add(index);
            String snippet = "fr.inria.spirals.asserts.log.Logger.log(";
            element.replace(
                    ctMethod.getFactory().createCodeSnippetStatement(snippet + index + "," + element.getArguments().get(1) + ")")
            );
        }
    }

    private static final Predicate<CtInvocation> isAssert = ctInvocation ->
            ctInvocation.getExecutable().getSimpleName().startsWith("assert") ||
                    isAssertionClass(ctInvocation.getExecutable().getDeclaringType().getActualClass());

    public static boolean isAssertionClass(Class klass) {
        return (klass.equals(org.junit.Assert.class) || klass.equals(junit.framework.Assert.class)) &&
                klass.getSuperclass() != null && isAssertionClass(klass.getSuperclass());
    }
}
