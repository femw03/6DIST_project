package origin.project.client.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import origin.project.client.Node;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.model.naming.dto.NodeRequest;

import java.io.IOException;
import java.net.*;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ShutdownService {
    @Autowired
    private Node node;
    @Autowired
    private MessageService messageService;
    Logger logger = Logger.getLogger(ShutdownService.class.getName());

    @PreDestroy
    public void shutdown() {
        System.out.println("\n Initiating shutdown process... \n");
        try {
            int previousID = node.getPreviousID();
            int currentID = node.getCurrentID();
            int nextID = node.getNextID();

            if (node.getExistingNodes() > 1) {
            // Next
                String URLnext = node.getNamingServerUrl() + "/get-IP-by-hash/" + nextID;
                String IPnext = messageService.getRequest(URLnext, "get next ip");
                // Because of GET request, IP is converted to string with extra double quotes ("")
                IPnext = IPnext.replace("\"", "");              // remove double quotes
                InetAddress IPnextInet =  InetAddress.getByName(IPnext);

            // Previous
                String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + previousID;
                String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
                // Because of GET request, IP is converted to string with extra double quotes ("")
                IPprevious = IPprevious.replace("\"", "");              // remove double quotes
                InetAddress IPpreviousInet =  InetAddress.getByName(IPprevious);

            // Sending
                messageService.sendMessage(IPnextInet,previousID,-1);
                messageService.sendMessage(IPpreviousInet,-1,nextID);
            }

            // remove mine
            String URLdelete = node.getNamingServerUrl() + "/remove-node/";
            String nodeBody = "{\"name\" : \"" + node.getNodeName() + "\", \"ip\" : \"" + node.getIpAddress() + "\"}" ;
            messageService.deleteRequest(URLdelete, nodeBody, "removeNode");
            node.setExistingNodes(node.getExistingNodes()-1);

        } catch (UnknownHostException e) {
            logger.warning("Error resolving hostname or IP during shutdown: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for detailed debugging
        }
    }
}
