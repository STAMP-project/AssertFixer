package eu.stamp.asserts;

import eu.stamp.AbstractTest;
import eu.stamp.EntryPoint;
import eu.stamp.Main;
import eu.stamp.runner.test.Failure;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtComment;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/05/17
 */
public class CommentTest extends AbstractTest {

    @Test
    public void testAddCommentWithOldAssertion() throws Exception {
        final String testCaseName = "testAssertionErrorBoolean";
        final String fullQualifiedName = "aPackage.ClassResourcesTest";

        assertTrue(spoon.getFactory().Class()
                .get(fullQualifiedName)
                .getMethodsByName(testCaseName)
                .get(0)
                .getBody()
                .getStatement(0)
                .getComments()
                .isEmpty()
        );

        List<Failure> failures = EntryPoint.runTests(
                getClasspath(),
                fullQualifiedName,
                testCaseName).getFailingTests();// 1st assert fail

        AssertFixer.fixAssert(spoon,
                fullQualifiedName,
                testCaseName,
                failures.get(0),
                getClasspath());

        final List<CtComment> comments = spoon.getFactory().Class()
                .get(fullQualifiedName)
                .getMethodsByName(testCaseName)
                .get(0)
                .getBody()
                .getStatement(0)
                .getComments();

        assertFalse(comments.isEmpty());
        final String commentGenerated = "// AssertFixer: old assertion Assert.assertTrue(ClassResourcesTest.getFalse())";
        assertEquals(commentGenerated, comments.get(0).toString());
    }

}
