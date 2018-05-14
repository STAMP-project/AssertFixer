package eu.stamp.project.assertfixer.asserts.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/05/17
 */
public class Logger {

    private static final String nameOfSerializedObservations = "observations.ser";

    public static Map<Integer, Object> observations = new HashMap<>();

    public static void log(int i, Object value) {
        if (observations.containsKey(i)) {
            if (!observations.get(i).equals(value)) {
                System.err.println("WARNING: this test may be flaky");
            }
        }
        observations.put(i, value);
    }

    public static void reset() {
        observations.clear();
    }

    public static void save() {
        try (FileOutputStream fout = new FileOutputStream(nameOfSerializedObservations)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                System.out.print(String.format("Saving %d observations...", observations.size()));
                oos.writeObject(observations);
                System.out.println(" Saved!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        try (FileInputStream fi = new FileInputStream(new File(nameOfSerializedObservations))) {
            try (ObjectInputStream oi = new ObjectInputStream(fi)) {
                observations = (Map) oi.readObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
