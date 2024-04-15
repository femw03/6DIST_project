package origin.project.server.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.repository.NamingRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DatabaseLoader {
    @Autowired
    private NamingRepository namingRepository;

    @Autowired
    private JsonService jsonService;

    private static final String FILE_PATH = "src/main/resources/nodes.json";
    private static final String DIRECTORY = "src/main/resources";
    private static final Logger logger = Logger.getLogger(DatabaseLoader.class.getName());


    public DatabaseLoader(NamingRepository namingRepository, JsonService jsonService) {
        this.namingRepository = namingRepository;
        this.jsonService = jsonService;
    }

    @PostConstruct
    private void initDatabase() {
        try {
            Path path = Path.of(FILE_PATH);
            if (Files.exists(path)) {
                List<NamingEntry> entries = jsonService.loadNamingEntriesFromJsonFile(FILE_PATH);
                if (entries != null) {
                    namingRepository.saveAll(entries);
                    logger.info("Loaded naming entries from JSON file.");
                }
            }
            else {
                Path dir = Path.of(DIRECTORY);
                if (Files.exists(dir)) {
                    Files.createFile(path);
                    logger.info("Created new JSON file.");
                }
                else {
                    Files.createDirectory(dir);
                    Files.createFile(path);
                    logger.info("Created right directory and JSON file.");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.severe("Error opening JSON file: " + e.getMessage());
        }
    }
}