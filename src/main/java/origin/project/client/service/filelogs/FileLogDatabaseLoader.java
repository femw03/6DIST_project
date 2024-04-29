package origin.project.client.service.filelogs;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.service.JsonServiceFileTransfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

@Service
public class FileLogDatabaseLoader {
    @Autowired
    private JsonServiceFileTransfer jsonService;
    @Autowired
    private FileLogRepository fileLogRepository;
    @Autowired
    private Node node;


    private static final Logger logger = Logger.getLogger(FileLogDatabaseLoader.class.getName());

    @PostConstruct
    private void initDatabase() {
        String FILE_PATH = node.getFILE_PATH();
        String DIRECTORY = node.getDIRECTORY();
        try {
            Path path = Path.of(FILE_PATH);
            if (Files.exists(path)) {
                List<FileLogEntry> entries = jsonService.loadNamingEntriesFromJsonFile(FILE_PATH);
                if (entries != null) {
                    fileLogRepository.saveAll(entries);
                    logger.info("Loaded file logs from JSON file.");
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
