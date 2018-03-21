package eu.stamp.asserts;

import spoon.reflect.code.CtInvocation;

import java.util.function.Function;
import java.util.function.Predicate;

import static eu.stamp.asserts.AssertionReplacer.isAssertionClass;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/03/18
 */
public class Utils {

    static final Function<Object, String> fieldOfObjectToString = (object) ->
            object.getClass().getSimpleName() + "." + object.toString();

    static final Predicate<Object> isFieldOfClass = (object) -> {
        try {
            return null != object.getClass().getField(object.toString());
        } catch (NoSuchFieldException e) {
            return false;
        }
    };

    static final Predicate<Object> isPrimitiveArray = obj ->
            Character.isLowerCase(obj.getClass().getSimpleName().toCharArray()[0]);

    static final Predicate<CtInvocation> isFail = ctInvocation ->
            ctInvocation.getExecutable().getSimpleName().startsWith("fail") ||
                    isAssertionClass(ctInvocation.getExecutable().getDeclaringType().getDeclaration());

}
