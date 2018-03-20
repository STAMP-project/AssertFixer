package eu.stamp;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/03/18
 */
public class Main {

    public static Configuration configuration = new Configuration();

    public static void main(String[] args) {
        Main.configuration = new Configuration(args);
    }

}
