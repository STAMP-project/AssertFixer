package eu.stamp.project.assertfixer.test;

import eu.stamp.project.assertfixer.Configuration;
import eu.stamp.project.assertfixer.asserts.log.Logger;
import eu.stamp.project.assertfixer.util.Util;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.TestListener;
import org.junit.AfterClass;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/05/17
 */
public class TestRunner {

    public static TestListener runTest(Configuration configuration, Launcher launcher, String failingTestClass, String failingTestMethod) {
        final SpoonModelBuilder compiler = launcher.createCompiler();
        compiler.setBinaryOutputDirectory(new File(configuration.getBinaryOutputDirectory()));
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        try {
            return EntryPoint.runTests(
                    configuration.getBinaryOutputDirectory()
                            + Util.PATH_SEPARATOR + configuration.getClasspath(),
                    failingTestClass,
                    failingTestMethod
            );
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runTestWithLogger(Configuration configuration, Launcher spoon,
                                         String classpath,
                                         String fullQualifiedName,
                                         String testCaseName) {
        final Factory factory = spoon.getFactory();
        final CtClass<?> testClass = factory.Class().get(fullQualifiedName);
        final CtElement addedElement = addSaveStatementInTearDownAfterClass(testClass);

        // TODO should compute the path to Logger.class using the class loader instead of hard coded value
        final String loggerClasspath = "target/classes/eu/stamp/project/assertfixer/log/Logger.class";
        final String binaryOutputDirectory = configuration.getBinaryOutputDirectory();
        final SpoonModelBuilder compiler = spoon.createCompiler();

        final CtMethod<?> ctMethod = testClass.getMethodsByName(testCaseName).get(0);

        List<CtMethod> addedMethod = new ArrayList<>();
        IntStream.range(0, 2)
                .forEach(index -> {
                    final CtMethod<?> clone = ctMethod.clone();
                    clone.setSimpleName(clone.getSimpleName() + "_" + index);
                    testClass.addMethod(clone);
                    addedMethod.add(clone);
                });
        compiler.setBinaryOutputDirectory(new File(configuration.getBinaryOutputDirectory()));
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);

        try {
            EntryPoint.runTests(binaryOutputDirectory + Util.PATH_SEPARATOR + classpath
                            + (new File(loggerClasspath).exists() ?
                            Util.PATH_SEPARATOR + loggerClasspath : ""),
                    fullQualifiedName,
                    testCaseName,
                    testCaseName + "_0",
                    testCaseName + "_1"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        addedMethod.forEach(testClass::removeMethod);

        if (addedElement instanceof CtMethod) {
            testClass.removeMethod((CtMethod) addedElement);
        } else {
            CtMethod<?> testDownAfterClass = testClass.filterChildren(new TypeFilter<CtMethod>(CtMethod.class) {
                @Override
                public boolean matches(CtMethod element) {
                    return element.getAnnotations().contains(factory.Annotation().get(AfterClass.class));
                }
            }).first();

            testDownAfterClass.getBody().removeStatement((CtStatement) addedElement);
        }
    }

    @SuppressWarnings("unchecked")
    private static CtElement addSaveStatementInTearDownAfterClass(CtClass<?> testClass) {
        final Factory factory = testClass.getFactory();
        CtMethod<?> testDownAfterClass = testClass.filterChildren(new TypeFilter<CtMethod>(CtMethod.class) {
            @Override
            public boolean matches(CtMethod element) {
                return element.getAnnotations().contains(factory.Annotation().get(AfterClass.class));
            }
        }).first();

        boolean methodCreated = false;
        if (testDownAfterClass == null) {
            methodCreated = true;
            testDownAfterClass = initializeTestDownAfterClassMethod(factory, testClass);
        }
        final CtType<?> loggerType = factory.Type().get(Logger.class);
        final CtMethod<?> save = loggerType.getMethodsByName("save").get(0);

        CtInvocation invocation = factory.createInvocation(factory.Code().createTypeAccess(loggerType.getReference()),
                save.getReference());
        testDownAfterClass.getBody().insertEnd(
                invocation
        );
        if (methodCreated) {
            return testDownAfterClass;
        } else {
            return invocation;
        }
    }

    private static CtMethod<?> initializeTestDownAfterClassMethod(Factory factory, CtClass<?> testClass) {
        final CtType<?> loggerType = factory.Type().get(Logger.class);
        final CtMethod<?> save = loggerType.getMethodsByName("save").get(0);
        final CtMethod tearDownAfterClass = factory.createMethod(
                testClass,
                new HashSet<>(Arrays.asList(ModifierKind.PUBLIC, ModifierKind.STATIC)),
                factory.Type().VOID_PRIMITIVE,
                "tearDownAfterClass",
                Collections.emptyList(),
                Collections.emptySet(),
                factory.createCtBlock(
                        factory.createInvocation(factory.Code().createTypeAccess(loggerType.getReference()),
                                save.getReference()))
        );
        final CtAnnotation annotation = factory.createAnnotation();
        final CtTypeReference reference = factory.Type().createReference(AfterClass.class);
        annotation.setAnnotationType(reference);
        tearDownAfterClass.addAnnotation(annotation);
        return tearDownAfterClass;
    }
}
