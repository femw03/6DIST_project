package origin.project.naming.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.naming.model.naming.NamingEntry;
import origin.project.naming.repository.NamingRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DatabaseLoader {
    @Autowired
    final private NamingRepository namingRepository;

    @Autowired
    final private JsonService jsonService;

    private static final String nodesFilePath = "resources/nodes.json";
    private static final String resourcesFolderPath = "resources";

    public DatabaseLoader(NamingRepository namingRepository, JsonService jsonService) {
        this.namingRepository = namingRepository;
        this.jsonService = jsonService;
    }

    @PostConstruct
    private void initDatabase() {
        try {
            Path path = Path.of(nodesFilePath);
            if (Files.exists(path)) {
                List<NamingEntry> entries = jsonService.loadNamingEntriesFromJsonFile(nodesFilePath);
                if (entries != null) {
                    namingRepository.saveAll(entries);
                }
            }
            else {
                Path dir = Path.of(resourcesFolderPath);
                if (Files.exists(dir)) {
                    Files.createFile(path);
                }
                else {
                   Files.createDirectory(dir);
                    Files.createFile(path);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }



//        NamingEntry localhost = new NamingEntry(1300, "localhost");
//        NamingEntry gones = new NamingEntry(200, "128.0.0.1");
//        NamingEntry femke = new NamingEntry(20000, "192.168.1.13");
//        NamingEntry nigel = new NamingEntry(32000, "192.168.2.56");
//        NamingEntry rien = new NamingEntry(28113, "192.168.1.42");
//        namingRepository.save(localhost);
//        namingRepository.save(gones);
//        namingRepository.save(femke);
//        namingRepository.save(nigel);
//        namingRepository.save(rien);
    }
}
