package test4repair.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/05/17
 */
public class JSONProject {

    public final String project;
    public final List<JSONBugID> JSONBugIDS;

    public JSONProject(String project, List<JSONBugID> jsonBugIDS) {
        this.project = project;
        JSONBugIDS = jsonBugIDS;
    }

    public JSONProject(String project) {
        this.project = project;
        this.JSONBugIDS = new ArrayList<>();
    }

    public JSONBugID getJSONBugID(String bugID) {
        return this.JSONBugIDS.stream()
                .filter(jsonBugID -> jsonBugID.bugID.equals(bugID))
                .findFirst()
                .orElse(null);
    }
}
