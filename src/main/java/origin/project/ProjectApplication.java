package origin.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.map.repository.config.EnableMapRepositories;
import origin.project.naming.map.NamingMap;

@SpringBootApplication
@EnableMapRepositories(mapType = NamingMap.class)
public class ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }

}


