package origin.project.client.agents;

import com.google.gson.Gson;
import jade.core.Agent;
import jade.core.behaviours.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import origin.project.client.Node;
import origin.project.client.model.dto.FailureAgentTransfer;
import origin.project.client.model.dto.LogEntry;
import origin.project.client.service.MessageService;
import origin.project.client.service.ReplicationService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class FailureAgent extends Agent {

    private Logger logger = Logger.getLogger(FailureAgent.class.getName());
    private Node node;
    private String IPFailingNode;
    private int IDStartingNode;

    private MessageService messageService;

    private ReplicationService replicationService;

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
            replicationService = (ReplicationService) args[4];

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

        // add the behavior to resolve the files
        addBehaviour(new ResolveFiles(this));
    }


    private class ResolveFiles extends OneShotBehaviour {

        public ResolveFiles(final Agent agent) {
            super(agent);
        }
        @Override
        public void action() {
            // resolve the local and replicated files
            try {
                replicationService.resolveFilesDuringFailure(IPFailingNode);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // The agent has done its job and can be deconstructed
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        logger.info("Shutting down failure agent with failing node IP: " + IPFailingNode + " and start ID: " + IDStartingNode);
        // Deconstruction of the agent
        if (node.getNextID() != IDStartingNode && node.getExistingNodes() > 1) {
            logger.info("Sending failure agent to next node.");
            try {
                sendFailureAgentToNextNode();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Failure agent reached the end of the loop, so it won't be send to the next node." );
        }
    }

    private void sendFailureAgentToNextNode() throws UnknownHostException{
        // Sometimes the nextID needs to converge (if not converged -> nextID = -1)
        // wait until it is converged
        try {
            while (node.getNextID() == -1) {
                logger.info("Waiting for next ID to converge");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get the correct URL
        String targetURL = node.getNamingServerUrl() + "/get-IP-by-hash/" + node.getNextID();
        String targetIPString = messageService.getRequest(targetURL, "get target ip");
        targetIPString = targetIPString.replace("\"", "");              // remove double quotes
        InetAddress targetIP = InetAddress.getByName(targetIPString);

        String failureAgentTransferUrl = "http:/" + targetIP + ":8080/failure-agent/run-failure-agent";


        // create the failureAgenTransfer-object and serialize
        Gson gson = new Gson();
        FailureAgentTransfer failureAgentTransfer = new FailureAgentTransfer(IPFailingNode, IDStartingNode);
        String failureAgentTransferJson = gson.toJson(failureAgentTransfer);

        String response = messageService.postRequest(failureAgentTransferUrl, failureAgentTransferJson, "run failure agent");
        logger.info(response);
    }
}