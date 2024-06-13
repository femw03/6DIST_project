package origin.project.server;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerRunnable {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();

        // create main-container on server
        Profile pMain = new ProfileImpl("localhost", 4242, "SystemY");
        pMain.setParameter(Profile.CONTAINER_NAME, "mainContainer");
        rt.createMainContainer(pMain);


        SpringApplication.run(ServerRunnable.class, args);
    }

}