package eu.stamp.project.assertfixer;

import eu.stamp.project.assertfixer.util.Util;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseDescription;
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
                "--classpath", AbstractTest.getJarPath(Test.class)+ Util.PATH_SEPARATOR + AbstractTest.getJarPath(BaseDescription.class),
                "--test-class", "aPackage.ClassResourcesTest",
                "--test-method", "testAssertionErrorBoolean:testAssertionErrorPrimitive",
                "--source-path", "src/test/resources/NotExist.java",
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
