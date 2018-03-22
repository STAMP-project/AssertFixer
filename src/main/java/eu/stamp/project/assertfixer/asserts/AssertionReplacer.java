package eu.stamp.project.assertfixer.asserts;

import eu.stamp.project.assertfixer.util.Util;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class AssertionReplacer {

    static List<Integer> replaceByLogStatement(CtMethod<?> clone) {
        final List<CtInvocation> assertionsToBeReplaced = clone.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return Util.isAssert.test(element) && super.matches(element);
            }
        }).stream()
                .sorted(Comparator.comparingInt(ctInvocation -> ctInvocation.getPosition().getLine()))
                .collect(Collectors.toList());
        final List<Integer> indices = new ArrayList<>();
        assertionsToBeReplaced.forEach(assertionToBeReplaced -> {
            final int index = assertionToBeReplaced.getParent(CtBlock.class).getStatements().indexOf(assertionToBeReplaced);
            indices.add(index);
            String snippet = "eu.stamp.project.assertfixer.asserts.log.Logger.log(";
            if (assertionToBeReplaced.getExecutable().getSimpleName().endsWith("True") ||
                    assertionToBeReplaced.getExecutable().getSimpleName().endsWith("False")) {
                assertionToBeReplaced.replace(
                        clone.getFactory().createCodeSnippetStatement(snippet + index + "," + assertionToBeReplaced.getArguments().get(0) + ")")
                );
            } else {
                assertionToBeReplaced.replace(
                        clone.getFactory().createCodeSnippetStatement(snippet + index + "," + assertionToBeReplaced.getArguments().get(1) + ")")
                );
            }
        });
        return indices;
    }


}
