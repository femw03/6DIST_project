package origin.project.client.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;


@Service
public class BootstrapService {
    @Autowired
    private Node node;
    @PostConstruct
    private void startUp() {
        try {

            MessageService.sendMulticastMessage(node.getNodeName(), node.getIpAddress());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
