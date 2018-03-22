package test4repair.json;

import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/05/17
 */
public class JSONSeed {

    public final String seed;
    public final List<JSONTest> tests;

    public JSONSeed(String seed, List<JSONTest> tests) {
        this.seed = seed;
        this.tests = tests;
    }

   public final static JSONSeed NO_BUG_EXPOSING = new JSONSeed("none", Collections.emptyList());

    @Override
    public boolean equals(Object o) {
        return o instanceof JSONSeed && this.seed.equals(((JSONSeed) o).seed) && super.equals(o);
    }
}
