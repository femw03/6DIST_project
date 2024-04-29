package origin.project.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class NodeRunnable {

    public static void main(String[] args) {

        // Check if enough arguments are provided
        /*if (args.length < 2) {
            System.out.println("Usage: java -jar your-application.jar <nodeName> <ipAddress>");
            return;
        }*/

        // Extract node name and IP address from command-line arguments
        //String nodeName = args[0];
        //String ipAddress = args[1];
        String nodeName = "testNode";
        String ipAddress = "localhost";

        // Set the arguments as system properties (optional but can be useful)
        System.setProperty("nodeName", nodeName);
        System.setProperty("ipAddress", ipAddress);
        System.setProperty("server.port", "-1");

        SpringApplication.run(origin.project.client.NodeRunnable.class, args);

    }

}