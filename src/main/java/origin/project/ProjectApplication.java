package origin.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import origin.project.naming.server.NamingServer;

@SpringBootApplication
//@EnableEurekaServer
public class ProjectApplication {
    @Bean
    public NamingServer namingServer() {
        return new NamingServer();
    }

    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }
}
