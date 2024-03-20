package origin.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
//import org.springframework.data.map.repository.config.EnableMapRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.data.map.repository.config.EnableMapRepositories;
import origin.project.naming.controller.NamingServerController;
import origin.project.naming.map.NamingMap;
import origin.project.naming.server.NamingServer;

import java.util.WeakHashMap;

@SpringBootApplication
//@ComponentScan("origin.project.naming") // test

@ComponentScan("origin.project.naming") //to scan packages mentioned
@EnableMapRepositories(mapType = NamingMap.class)
public class ProjectApplication {
    /*@Bean
    public NamingServer namingServer() {
        return new NamingServer();
    }*/

    @Bean
    public KeyValueOperations keyValueTemplate() {
        return new KeyValueTemplate(keyValueAdapter());
    }

    @Bean
    public KeyValueAdapter keyValueAdapter() {
        return new MapKeyValueAdapter(WeakHashMap.class);
    }

    public static void main(String[] args) {
        new NamingServerController();
        SpringApplication.run(ProjectApplication.class, args);
    }
}
