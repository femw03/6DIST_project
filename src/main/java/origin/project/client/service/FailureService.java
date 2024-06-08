package origin.project.client.service;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.agents.FailureAgent;
import origin.project.client.agents.SyncAgent;

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
        logger.info("Existing nodes: "+node.getExistingNodes());

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
            node.setPingEnable(true);

        } else if (Objects.equals(IPaddress, IPprevious)) {
            node.setPreviousID(-1);
            String message = "Discover previous," + node.getCurrentID();
            // Wait until everyone discovered failed connection
            Thread.sleep(10000); // 10 seconds delay
            messageService.sendMulticastMessage(message);

        } else if (Objects.equals(IPaddress, IPnext)) {
            node.setNextID(-1);
            String message = "Discover next," + node.getCurrentID();
            // Wait until everyone discovered failed connection
            Thread.sleep(10000); // 10 seconds delay
            messageService.sendMulticastMessage(message);
        }

        // Remove failed node
        String URLnode = node.getNamingServerUrl() + "/get-node/" + IPaddress;
        boolean nodeExists = Boolean.parseBoolean(messageService.getRequest(URLnode, "get node"));

        if (nodeExists) {
            String URLhash = node.getNamingServerUrl() + "/get-hash-by-IP/" + IPaddress;
            String hashIDString = messageService.getRequest(URLhash, "get hashID");
            logger.info("Removing node "+IPaddress);
            int hashID = Integer.parseInt(hashIDString);
            String URLdelete = node.getNamingServerUrl() + "/remove-node";
            String nodeBody = "{\"hash\" : \"" + hashID + "\", \"ip\" : \"" + IPaddress + "\"}" ;
            messageService.deleteRequest(URLdelete, nodeBody, "removeNode");
        } else {
            logger.info("Node "+IPaddress+" already removed");
        }

        // Start the FailureAgent
        try {
            startFailureAgent(IPaddress);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }


    private void startFailureAgent(String IPadress) throws StaleProxyException {
        // create a FailureAgent in the container.
        AgentController controller = node.getMainContainer().createNewAgent(node.getNodeName() + "failure agent", FailureAgent.class.getName(), new Object[] {IPadress, node.getCurrentID(), node, messageService});
        // start the FailureAgent.
        controller.start();
    }
}
