package origin.project.client.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;


@Service
public class BootstrapService {
    @Autowired
    private Node node;

    @Autowired
    private MessageService messageService;
    Logger logger = Logger.getLogger(BootstrapService.class.getName());

    @PostConstruct
    private void startUp() {
        try {
            String message = "newNode," + node.getNodeName() + "," + node.getIpAddress(); // Combine name and IP address
            messageService.sendMulticastMessage(message);
            node.setLocalLog(new HashMap<>());
            node.setReplicatedLog(new HashMap<>());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
