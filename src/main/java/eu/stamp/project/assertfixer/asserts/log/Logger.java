package eu.stamp.project.assertfixer.asserts.log;

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
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream(nameOfSerializedObservations);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(observations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ignored) {

                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        try {
            fin = new FileInputStream(nameOfSerializedObservations);
            ois = new ObjectInputStream(fin);
            observations = (Map) ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ignored) {
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ignored) {
                }
            }

        }
    }

}
