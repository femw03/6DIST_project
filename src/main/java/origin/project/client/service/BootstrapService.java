package origin.project.client.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
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
            messageService.sendMulticastMessage(node.getNodeName(), node.getIpAddress());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
