package origin.project.naming.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class DatabaseLoader {
    @Autowired
    private NodeRepository nodeRepository;

    private static final String FILE_PATH = "nodes.json";

    public DatabaseLoader(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @PostConstruct
    private void initDatabase() {
        // Create sample node and save it to the repository
        //Node localhost = new Node("Node1", "localhost");
        //nodeRepository.save(new NamingEntry(localhost));

        // Load existing entries from JSON file if it exists
        loadEntriesFromJsonFile();
    }

    private void loadEntriesFromJsonFile() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<NamingEntry> entries = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, NamingEntry.class));
                nodeRepository.saveAll(entries);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("JSON file does not exist. No entries loaded.");
        }
    }

    public static void updateEntriesToJsonFile(NodeRepository nodeRepository) {
        Iterable<NamingEntry> entries = nodeRepository.findAll();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(FILE_PATH);
            objectMapper.writeValue(file, entries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}