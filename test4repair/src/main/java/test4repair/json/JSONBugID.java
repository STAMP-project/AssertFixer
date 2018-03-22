package test4repair.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/05/17
 */
public class JSONBugID {

    public final String bugID;
    public final List<JSONSeed> seeds;

    public JSONBugID(String bugID, List<JSONSeed> seeds) {
        this.bugID = bugID;
        this.seeds = seeds;
    }

    public JSONBugID(String bugID) {
        this.bugID = bugID;
        this.seeds = new ArrayList<>();
    }
}
