package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class FailureService {
    @Autowired
    private MessageService messageService;
    @Autowired
    Node node;
    Logger logger = Logger.getLogger(FailureService.class.getName());

    public void Failure(String IPaddress) throws UnknownHostException, InterruptedException {
        int nextID = node.getNextID();
        int previousID = node.getPreviousID();

        // next
        String URLnext = node.getNamingServerUrl() + "/get-IP-by-hash/" + nextID;
        String IPnext = messageService.getRequest(URLnext, "get next ip");
        IPnext = IPnext.replace("\"", "");              // remove double quotes

        // previous
        String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + previousID;
        String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
        IPprevious = IPprevious.replace("\"", "");              // remove double quotes

        if (Objects.equals(IPaddress, IPprevious) && Objects.equals(IPaddress, IPnext)) {       // only 1 node left in network
            node.setPreviousID(node.getCurrentID());
            node.setNextID(node.getCurrentID());
            logger.info("Previous ID: " + node.getPreviousID());
            logger.info("Current ID: " + node.getCurrentID());
            logger.info("Next ID: " + node.getNextID());

        } else if (Objects.equals(IPaddress, IPprevious)) {
            node.setPreviousID(-1);
            // send multicast
            String message = "Discover previous," + node.getCurrentID();
            // Wait until everyone discovered failed connection
            Thread.sleep(10000); // 10 seconds delay
            messageService.sendMulticastMessage(message);

        } else if (Objects.equals(IPaddress, IPnext)) {
            node.setNextID(-1);
            // send multicast
            String message = "Discover next," + node.getCurrentID();
            // Wait until everyone discovered failed connection
            Thread.sleep(10000); // 10 seconds delay
            messageService.sendMulticastMessage(message);
        }

        // remove failed node
        String URLhash = node.getNamingServerUrl() + "/get-hash-by-IP/" + IPaddress;
        int hashID = Integer.parseInt(messageService.getRequest(URLhash, "get hashID"));

        String URLdelete = node.getNamingServerUrl() + "/remove-node";
        String nodeBody = "{\"hash\" : \"" + hashID + "\", \"ip\" : \"" + IPaddress + "\"}" ;
        messageService.deleteRequest(URLdelete, nodeBody, "removeNode");
        node.setExistingNodes(node.getExistingNodes()-1);
    }

}
