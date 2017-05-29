package fr.inria.spirals.json;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/05/17
 */
public class JSONTest {

    public final String name;
    public final boolean repaired;
    public final String failure;
    public final boolean nofailure;
    public final boolean timeout;

    public JSONTest(String name, boolean repaired, String failure, boolean nofailure, boolean timeout) {
        this.name = name;
        this.repaired = repaired;
        this.failure = failure;
        this.nofailure = nofailure;
        this.timeout = timeout;
    }
}
