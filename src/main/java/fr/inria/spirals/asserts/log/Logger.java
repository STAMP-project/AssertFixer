package fr.inria.spirals.asserts.log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class Logger {

    public static final Map<Integer, Object> observations = new HashMap<>();

    public static void log(int i, Object value) {
        observations.put(i, value);
    }

    public static void reset() {
        observations.clear();
    }

}
