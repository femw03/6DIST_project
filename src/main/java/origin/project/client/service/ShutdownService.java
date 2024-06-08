package origin.project.client.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

@Service
public class ShutdownService {
    @Autowired
    private Node node;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ReplicationService replicationService;

    Logger logger = Logger.getLogger(ShutdownService.class.getName());
    /*
    @PreDestroy
    public void shutdown() {
        System.out.println("\n Initiating shutdown process... \n");
        try {
            int previousID = node.getPreviousID();
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

            // Update files

                // Transfer file log of every replicated node to new node + update
                System.out.println("message shutdown send !!!");
                messageService.sendMulticastMessage("shutting down");

                // Sending
                if (previousID == nextID) {                                         // Only 2 nodes in network
                    messageService.sendMessage(IPnextInet, -1, -1);
                } else {
                    messageService.sendMessage(IPnextInet, previousID, -1);
                    messageService.sendMessage(IPpreviousInet, -1, nextID);
                }
                replicationService.processShuttingDown(IPpreviousInet);
            }

            // remove node
            String URLdelete = node.getNamingServerUrl() + "/remove-node";
            String nodeBody = "{\"name\" : \"" + node.getNodeName() + "\", \"ip\" : \"" + node.getIpAddress().getHostAddress() + "\"}" ;
            messageService.deleteRequest(URLdelete, nodeBody, "removeNode");
            node.setExistingNodes(node.getExistingNodes()-1);

        } catch (UnknownHostException e) {
            logger.warning("Error resolving hostname or IP during shutdown: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for detailed debugging
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    } */
    // moved replication-shutdownProcess to replicationService.
}
