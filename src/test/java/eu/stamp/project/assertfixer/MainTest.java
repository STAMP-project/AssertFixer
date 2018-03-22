package eu.stamp.project.assertfixer;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/18
 */
public class MainTest {

    @Test
    public void test() throws Exception {
        assertEquals(0, Main.run(new String[]{
                "--classpath", AbstractTest.dependenciesToRunJUnit,
                "--test-class", "aPackage.ClassResourcesTest",
                "--test-method", "testAssertionErrorBoolean:testAssertionErrorPrimitive",
                "--source-path", "src/test/resources/ClassResourcesTest.java",
                "--test-path", "src/test/resources/ClassResourcesTest.java",
                "--verbose",
                "--output", "target/assert-fixer"
        }));
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.forceDelete(new File("target/assert-fixer"));
    }
}
