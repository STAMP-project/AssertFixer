package fr.inria.spirals.asserts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
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

        @Override
        public void process(CtInvocation element) {
            final int index = element.getParent(CtBlock.class).getStatements().indexOf(element);
            final CtMethod<?> ctMethod = element.getParent(CtMethod.class);
            indexToBeLogged.add(index);
            String snippet = "fr.inria.spirals.asserts.log.Logger.log(";
            if(element.getExecutable().getSimpleName().endsWith("True") ||
                    element.getExecutable().getSimpleName().endsWith("False")) {
                element.replace(
                        ctMethod.getFactory().createCodeSnippetStatement(snippet + index + "," + element.getArguments().get(0) + ")")
                );
            } else {
                element.replace(
                        ctMethod.getFactory().createCodeSnippetStatement(snippet + index + "," + element.getArguments().get(1) + ")")
                );
            }
        }
    }

    private static final Predicate<CtInvocation> isAssert = ctInvocation ->
            ctInvocation.getExecutable().getSimpleName().startsWith("assert") ||
                    isAssertionClass(ctInvocation.getExecutable().getDeclaringType().getDeclaration());

    public static boolean isAssertionClass(CtType klass) {
        return klass != null &&
                ("org.junit.Assert".equals(klass.getQualifiedName()) || "junit.framework.Assert".equals(klass.getQualifiedName())) &&
                klass.getSuperclass() != null && isAssertionClass(klass.getSuperclass().getDeclaration());
    }
}
