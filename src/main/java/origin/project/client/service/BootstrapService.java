package origin.project.client.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.multicast.MulticastService;

import java.io.IOException;


@Service
public class BootstrapService {
    @Autowired
    private Node node;
    @PostConstruct
    private void startUp() {
        try {

            MulticastService.sendMulticastMessage(node.getNodeName(), node.getIpAddress());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
