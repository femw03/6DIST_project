package origin.project.server.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.repository.NamingRepository;
import origin.project.server.service.JsonService;
import origin.project.server.service.NamingService;

import java.io.IOException;

@Service
public class MulticastReceiver {
    @Autowired
    private NamingService namingService;
    @Autowired
    private JsonService jsonService;
    @Autowired
    private NamingRepository namingRepository;
    @Autowired
    private NamingServerController namingServerController;

    private static final String FILE_PATH = "src/main/resources/nodes.json";

    public void receiveMulticastMessage(String multicastMessage) throws IOException {

        // Parse multicast message and extract node name, IP address
        String[] parts = multicastMessage.split(",");

        if (parts.length != 2) {
            throw new IOException("Invalid multicast message format");
        }

        String nodeName = parts[0];
        String ipAddress = parts[1];

        // Calculate hash based on node name
        int hash = namingService.hashingFunction(nodeName);

        // Add hash and IP address to map data structure
        NamingEntry namingEntry = new NamingEntry(hash, ipAddress);
        jsonService.addEntryToJsonFile(FILE_PATH, namingEntry);
        namingRepository.save(namingEntry);

        // Respond to new node with number of existing nodes
        int existingNodesCount = (int) namingRepository.count();

        System.out.println("Multicast message handled successfully. Existing nodes count: " + existingNodesCount);

        namingServerController.respondToMulticast(existingNodesCount);

    }
}
