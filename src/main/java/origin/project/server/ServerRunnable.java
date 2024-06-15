package origin.project.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import origin.project.GUI.NetworkMonitorApp;

@SpringBootApplication
public class ServerRunnable {
    public static void main(String[] args) {
        // Start the naming server
        SpringApplication.run(ServerRunnable.class, args);
    }
}