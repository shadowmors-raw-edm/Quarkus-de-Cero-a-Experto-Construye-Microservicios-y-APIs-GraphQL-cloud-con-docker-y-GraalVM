package rest_client_02;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Qualifier;

import java.util.Arrays;

@QuarkusMain
public class CustomMain {

    public static void main(String[] args) {
        /*System.out.println("args = " + Arrays.toString(args));
        System.out.println("Running in another main");
        Quarkus.run(args);*/
        Quarkus.run(CustomApp.class, args);
    }

    public static class CustomApp implements QuarkusApplication {

        @Override
        public int run(String... args) {
            System.out.println("Running in another main as application");
            Quarkus.waitForExit();
            return 0;
        }
    }
}
