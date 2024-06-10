/*package origin.project.client.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class FailureAgent extends Agent {

    private String failingNodeName;
    private String newOwnerName;

    protected void setup() {
        // Get arguments passed to the agent
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            failingNodeName = (String) args[0];
            newOwnerName = (String) args[1];
            // Add behavior to the agent
            addBehaviour(new TransferOwnershipBehavior());
        } else {
            System.out.println("Error: Specify the failing node name and new owner name as arguments.");
            doDelete(); // Terminate the agent if arguments are not provided
        }
    }

    protected void takeDown() {
        // Clean up resources if needed
    }

    private class TransferOwnershipBehavior extends OneShotBehaviour {

        @Override
        public void action() {
            // Check if there are files owned by the failing node on this node
            // For simplicity, assume there is a list of files owned by each node
            String[] filesOwnedByThisNode = {"file1.txt", "file2.txt", "file3.txt"}; // Example list of files
            for (String fileName : filesOwnedByThisNode) {
                if (fileName.startsWith(failingNodeName)) {
                    // Transfer ownership of the file to the new owner
                    transferOwnership(fileName);
                    // Update the log file to reflect the ownership transfer
                    updateLogFile(fileName);
                }
            }
        }

        private void transferOwnership(String fileName) {
            // Logic to transfer ownership of the file to the new owner
            // You can implement communication with the new owner node here
            // Example: Send a message to the new owner's agent to inform about ownership transfer
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(newOwnerName, AID.ISLOCALNAME));
            msg.setContent("Transfer ownership of file: " + fileName);
            send(msg);
        }

        private void updateLogFile(String fileName) {
            // Logic to update the log file with the ownership transfer information
            System.out.println("Updated log file: Ownership of file '" + fileName + "' transferred from "
                    + failingNodeName + " to " + newOwnerName);
        }
    }
}*/
package origin.project.client.agents;

import com.google.gson.Gson;
import jade.core.Agent;
import jade.core.behaviours.*;
import org.springframework.beans.factory.annotation.Autowired;
import origin.project.client.Node;
import origin.project.client.model.dto.FailureAgentTransfer;
import origin.project.client.service.MessageService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class FailureAgent extends Agent {

    private Logger logger = Logger.getLogger(FailureAgent.class.getName());
    private Node node;
    private String IPFailingNode;
    // We need to know where the FailureAgent started, so we can terminate it once it has looped over all the nodes
    private int IDStartingNode;

    private MessageService messageService;

    @Override
    protected void setup() {
        // Construction of the agent
        super.setup();
        logger.info("Setup failure agent");
        Object[] args = getArguments();
        if(args!=null){
            IPFailingNode = (String) args[0];
            IDStartingNode = (Integer) args[1];
            node = (Node) args[2];
            messageService = (MessageService) args[3];
            if (IPFailingNode == null) {
                logger.info("IP of the failing node is null");
            }
            if (node == null) {
                logger.info("node is null");
            }
        }else{
            System.err.println("Error during parameter transfer");
            System.exit(0);
        }
        logger.info("Finished setup failure agent with failing node IP: " + IPFailingNode + " and start ID: " + IDStartingNode);

        addBehaviour(new ResolveFilesOwnedByFailingNode(this));
    }


    private class ResolveFilesOwnedByFailingNode extends OneShotBehaviour {

        public ResolveFilesOwnedByFailingNode(final Agent agent) {
            super(agent);
        }
        @Override
        public void action() {
            // Scan the file log for any files that the failed node had and update them
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        logger.info("Shutting down failure agent with failing node IP: " + IPFailingNode + " and start ID: " + IDStartingNode);
        // Deconstruction of the agent
        if (node.getNextID() != IDStartingNode && node.getExistingNodes() > 1) {
            logger.info("Sending failure agent to next node");
            try {
                sendFailureAgentToNextNode();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Failure agent reached the end of the loop, so it won't be send to the next node." );
        }
    }

    private void sendFailureAgentToNextNode() throws UnknownHostException {
        String targetURL = node.getNamingServerUrl() + "/get-IP-by-hash/" + node.getNextID();
        String targetIPString = messageService.getRequest(targetURL, "get target ip");
        targetIPString = targetIPString.replace("\"", "");              // remove double quotes
        InetAddress targetIP = InetAddress.getByName(targetIPString);

        String failureAgentTransferUrl = "http:/" + targetIP + ":8080/failure-agent/run-failure-agent";


        // create Filetransfer-object and serialize
        Gson gson = new Gson();
        FailureAgentTransfer failureAgentTransfer = new FailureAgentTransfer(IPFailingNode, IDStartingNode);
        String failureAgentTransferJson = gson.toJson(failureAgentTransfer);

        logger.info("Sending " + failureAgentTransfer + " to: " + failureAgentTransferUrl);

        String response = messageService.postRequest(failureAgentTransferUrl, failureAgentTransferJson, "run failure agent");
        logger.info(response);
    }
}