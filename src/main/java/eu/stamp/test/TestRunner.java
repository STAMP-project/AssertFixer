package eu.stamp.test;

import eu.stamp.EntryPoint;
import eu.stamp.Main;
import eu.stamp.asserts.log.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/05/17
 */
public class TestRunner {

    public static void runTest() {

    }

    public static void runTestWithLogger(Launcher spoon,
                                         String classpath,
                                         String fullQualifiedName,
                                         String testCaseName) {
        final Factory factory = spoon.getFactory();
        final CtClass<?> testClass = factory.Class().get(fullQualifiedName);
        addSaveStatementInTearDownAfterClass(testClass);

        final String loggerClasspath = ":target/classes/eu/stamp/asserts/log/Logger.class";
        final SpoonModelBuilder compiler = spoon.createCompiler();

        final CtMethod<?> ctMethod = testClass.getMethodsByName(testCaseName).get(0);
        IntStream.range(0, 2)
                .forEach(index -> {
                    final CtMethod<?> clone = ctMethod.clone();
                    clone.setSimpleName(clone.getSimpleName() + "_" + index);
                    testClass.addMethod(clone);
                });
        compiler.setBinaryOutputDirectory(new File(Main.configuration.getBinaryOutputDirectory()));
        compiler.compile(SpoonModelBuilder.InputType.CTTYPES);
        EntryPoint.runTests(classpath + loggerClasspath,
                fullQualifiedName,
                testCaseName,
                testCaseName + "_0",
                testCaseName + "_1"
        );
    }

    @SuppressWarnings("unchecked")
    private static void addSaveStatementInTearDownAfterClass(CtClass<?> testClass) {
        final Factory factory = testClass.getFactory();
        CtMethod<?> testDownAfterClass = testClass.filterChildren(new TypeFilter<CtMethod>(CtMethod.class) {
            @Override
            public boolean matches(CtMethod element) {
                return element.getAnnotations().contains(factory.Annotation().get(AfterClass.class));
            }
        }).first();
        if (testDownAfterClass == null) {
            testDownAfterClass = initializeTestDownAfterClassMethod(factory, testClass);
        }
        final CtType<?> loggerType = factory.Type().get(Logger.class);
        final CtMethod<?> save = loggerType.getMethodsByName("save").get(0);
        testDownAfterClass.getBody().insertEnd(
                factory.createInvocation(factory.Code().createTypeAccess(loggerType.getReference()),
                        save.getReference())
        );
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
