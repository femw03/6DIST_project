package origin.project.server.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.server.repository.NamingRepository;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class DatabaseLoader {
    @Autowired
    private NamingRepository namingRepository;

    @Autowired
    private JsonService jsonService;

    private static final String FILE_PATH = "src/main/java/origin/project/server/service/nodes.json";
    private static final Logger logger = Logger.getLogger(DatabaseLoader.class.getName());


    public DatabaseLoader(NamingRepository namingRepository, JsonService jsonService) {
        this.namingRepository = namingRepository;
        this.jsonService = jsonService;
    }

    @PostConstruct
    private void initDatabase() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            namingRepository.saveAll(jsonService.loadNamingEntriesFromJsonFile(FILE_PATH));
            logger.info("Loaded naming entries from JSON file.");
        } else {
            try {
                if (file.createNewFile()) {
                    logger.info("Created new JSON file.");
                } else {
                    logger.warning("Failed to create JSON file.");
                }
            } catch (IOException e) {
                logger.severe("Error creating JSON file: " + e.getMessage());
            }
        }
    }
}