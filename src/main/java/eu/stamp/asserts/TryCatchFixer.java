package eu.stamp.asserts;

import eu.stamp.runner.test.Failure;
import spoon.Launcher;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/03/18
 */
public class TryCatchFixer {

    @SuppressWarnings("unchecked")
    static void fixTryCatchFailAssertion(Launcher spoon, CtMethod<?> testCaseToBeFix, Failure failure, List<CtCatch> catches) {
        final String[] splittedNameException = failure.fullQualifiedNameOfException.split("\\.");
        String exceptionName = splittedNameException[splittedNameException.length - 1];
        final CtTypeReference reference = testCaseToBeFix.getFactory().Type().createReference(failure.fullQualifiedNameOfException);
        final CtCatchVariable<? extends Throwable> catchVariable = testCaseToBeFix.getFactory().createCatchVariable(
                reference,
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
                    spoon.getFactory().createLiteral(PREFIX_MESSAGE_EXPECTED_EXCEPTION + exceptionName)
            );
        }
    }

    static void addTryCatchFailAssertion(Launcher spoon, CtMethod<?> testCaseToBeFix, Failure failure) {
        final Factory factory = spoon.getFactory();
        final CtTry aTry = factory.createTry();
        final CtCatch aCatch = factory.createCatch();
        aTry.addCatcher(aCatch);
        final String[] splittedNameException = failure.fullQualifiedNameOfException.split("\\.");
        String exceptionName = splittedNameException[splittedNameException.length - 1];
        final CtCatchVariable catchVariable = factory.createCatchVariable(
                factory.Type().createReference(failure.fullQualifiedNameOfException),
                PREFIX_NAME_EXPECTED_EXCEPTION + exceptionName
        );
        aCatch.setParameter(catchVariable);
        aCatch.setBody(factory.createCodeSnippetStatement(
                "org.junit.Assert.assertTrue(true)"
        ));
        aTry.setBody(testCaseToBeFix.getBody().getStatement(0));
        for (int i = 1; i < testCaseToBeFix.getBody().getStatements().size(); i++) {
            aTry.getBody().addStatement(testCaseToBeFix.getBody().getStatement(i));
        }
        aTry.getBody().addStatement(factory
                .createCodeSnippetStatement("org.junit.Assert." + NAME_FAIL_METHOD + "(\"" + PREFIX_MESSAGE_EXPECTED_EXCEPTION + exceptionName + "\")")
        );
        testCaseToBeFix.setBody(aTry);
    }

    static final String PREFIX_NAME_EXPECTED_EXCEPTION = "expectedException__";
    static final String PREFIX_MESSAGE_EXPECTED_EXCEPTION = "Expecting exception: ";
    static final String NAME_FAIL_METHOD = "fail";

}
