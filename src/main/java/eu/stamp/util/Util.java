package eu.stamp.util;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class Util {

    public static final List<String> assertionErrors = new ArrayList<>();

    static {
        assertionErrors.add("java.lang.AssertionError");
        assertionErrors.add("org.junit.internal.ArrayComparisonFailure");
    }

    public static final Function<Object, String> fieldOfObjectToString = (object) ->
            object.getClass().getSimpleName() + "." + object.toString();

    public static final Predicate<Object> isFieldOfClass = (object) -> {
        try {
            return null != object.getClass().getField(object.toString());
        } catch (NoSuchFieldException e) {
            return false;
        }
    };

    public static final Predicate<Object> isPrimitiveArray = obj ->
            Character.isLowerCase(obj.getClass().getSimpleName().toCharArray()[0]);

    public static final Predicate<CtInvocation> isFail = ctInvocation ->
            ctInvocation.getExecutable().getSimpleName().startsWith("fail") ||
                    isAssertionClass(ctInvocation.getExecutable().getDeclaringType().getDeclaration());

    public static final Predicate<CtInvocation> isAssert = ctInvocation ->
            ctInvocation.getExecutable().getSimpleName().startsWith("assert") ||
                    isAssertionClass(ctInvocation.getExecutable().getDeclaringType().getDeclaration());

    public static boolean isAssertionClass(CtType klass) {
        return klass != null &&
                ("org.junit.Assert".equals(klass.getQualifiedName()) || "junit.framework.Assert".equals(klass.getQualifiedName())) &&
                klass.getSuperclass() != null && isAssertionClass(klass.getSuperclass().getDeclaration());
    }

    public static final String EXTENSION_JSON = ".json";

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final String EXTENSION_JAVA = ".java";

    public static final Predicate<File> IS_JSON_FILE = file -> (file.getName().endsWith(EXTENSION_JSON));

    public static final Predicate<File> IS_JAVA_FILE = file -> (file.getName().endsWith(EXTENSION_JAVA));

    public static final Predicate<File> CONTAINS_JAVA_FILE = file ->
            file.listFiles() != null &&
                    (Arrays.stream(file.listFiles()).anyMatch(IS_JAVA_FILE));

    public static final Function<File, String> JSON_FILE_TO_STRING_WITHOUT_EXTENSION = file ->
            (file.getName().substring(0, file.getName().length() - EXTENSION_JSON.length()));

    public static String extractDigitAtEndOfString(String string) {
        StringBuilder digits = new StringBuilder();
        int index = string.length() - 1;
        final char[] chars = string.toCharArray();
        while (Character.isDigit(chars[index])) {
            digits.append(chars[index]);
            index--;
        }
        return digits.reverse().toString();
    }

}
