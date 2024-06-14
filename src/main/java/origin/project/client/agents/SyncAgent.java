package origin.project.client.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import origin.project.client.Node;
import origin.project.client.service.ReplicationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SyncAgent extends Agent {

    private Node node;

    private ConcurrentHashMap<String, Boolean> fileMap;

    private ReplicationService replicationService;

    Logger logger = Logger.getLogger(SyncAgent.class.getName());

    protected void setup() {
        super.setup();
        logger.info("Setup");
        Object[] args = getArguments();
        if(args!=null){
            node = (Node) args[0];
            if (node == null) {
                logger.info("Node is null");
            }

            replicationService = (ReplicationService) args[1];
            if (replicationService == null) {
                System.err.println("Agent setup - replication service null");
                System.exit(0);
            }
            fileMap = convertFileListToMap(replicationService.getCurrentLocalFiles());
            node.setNodeFileMap(fileMap);


        }
        else {
            System.err.println("Error during parameter transfer SyncAgent");
            System.exit(0);
        }
        logger.info("Setting args successful for Sync Agent Node " + node.getNodeName());


        // add TickerBehavior that schedules the behavior to check the files.
        addBehaviour(new RequestFileMapBehavior(this, 6000));
        addBehaviour(new MessageProcessingBehaviour(this));
    }

    // agent clean-up operations
    protected void takeDown () {

    }

    /**
     * Behavior to ask the agent of the next node their file list.
     * TickerBehavior to repeat request after a given period.
     */
    private class RequestFileMapBehavior extends TickerBehaviour {
        public RequestFileMapBehavior(final Agent agent, long periodMilliSec) {
            super(agent, periodMilliSec);
            logger.info("Constructor RequestFileMapBehavior");
        }
        @Override
        protected void onTick() {
            // if another node is present in the network
            logger.info("file map before update " + fileMap);
            updateFileMapWithFileList();
            logger.info("file map after update " + fileMap);

            System.out.println(node.getExistingNodes());
            boolean nextNodeAvailable = node.getExistingNodes() > 1
                    && (node.getNextID() != node.getCurrentID() || node.getNextID() != -1);

            if (nextNodeAvailable) {
                logger.info("RequestFileMap - nextNodeAvailable");
                // Create a message
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

                // Set the sender and the receiver
                msg.setSender(this.myAgent.getAID());
                String agentName = "syncAgent" + node.getNextID();
                msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));

                // Set the content of the message.
                msg.setContent("fileMap-request");

                // send the message
                this.myAgent.send(msg);
            }
        }
    }

    private class MessageProcessingBehaviour extends CyclicBehaviour {
        public MessageProcessingBehaviour(Agent agent) {
            super(agent);
        }
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            logger.info("MessageProcessingBehavior");

            if (msg != null) {
                logger.info("MessageProcessingBehavior - msg not null");
                // use performative to filter on request or inform
                System.out.println(msg.getPerformative());
                switch(msg.getPerformative()) {
                    case ACLMessage.REQUEST:
                        logger.info("MessageProcessingBehavior - Request");
                        handleRequestMessage(msg);
                        break;
                    case ACLMessage.FAILURE:
                        logger.info("ACL Message Failure - " + msg.getContent());
                        break;
                    case ACLMessage.CONFIRM:
                        logger.info("MessageProcessingBehavior - CONFIRM");
                        handleConfirmMessage(msg);
                        break;
                }
            } else {
                // If no message is received, block the behaviour to save CPU cycles
                block();
            }
        }
    }

    /**
     * Update the Agent's file map with the changes in the replication file list.
     */
    public void updateFileMapWithFileList() {
        ArrayList<String> fileList = replicationService.getCurrentLocalFiles();

        // if file in fileList, not in Map = add to fileMap.
        for (String name : fileList) {
            if (!fileMap.containsKey(name)) {
                fileMap.put(name, false);
            }
        }

        // if file in Map, not in fileList = remove from fileMap.
        for (String name : fileMap.keySet()) {
            if (!fileList.contains(name)) {
                fileMap.remove(name);
            }
        }
    }

    public ConcurrentHashMap<String, Boolean> castObjectToMapStringBoolean(Object object) {
        if (object instanceof ConcurrentHashMap<?, ?> tempMap) {
            ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<>();

            for (ConcurrentHashMap.Entry<?, ?> entry : tempMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Boolean) {
                    map.put((String) entry.getKey(), (Boolean) entry.getValue());
                } else {
                    System.out.println("Non-string key or non-boolean value found in the map.");
                }
            }
            return map;
        } else {
            System.out.println("Argument is not a ConcurrentHashMap.");
            return null;
        }
    }

    public ConcurrentHashMap<String, Boolean> convertFileListToMap(ArrayList<String> arrayList) {
        ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<>();
        if (arrayList.isEmpty()) {
            logger.info("Initial file list has size 0. Returning empty map");
            return map;
        }

        for (String element : arrayList) {
            map.put(element, false);
        }
        return map;
    }

    public void handleConfirmMessage(ACLMessage msg) {
        //String content = msg.getContent();
//        System.out.println("Agent " + getLocalName() + " received CONFIRM: " + content);

        Object contentObject;
        try {
            contentObject = msg.getContentObject();
            ConcurrentHashMap<String, Boolean> neighborFileMap = castObjectToMapStringBoolean(contentObject);
            if (neighborFileMap != null) {

                System.out.println(neighborFileMap);
                System.out.println("Old file map: " + fileMap);

                for (String fileNeighbor : neighborFileMap.keySet()) {
                    if (fileMap.containsKey(fileNeighbor)) {
                        // If our fileMap contains file, check the locks.
                        // perform OR : true || false, false || true, true || true == true
                        fileMap.put(fileNeighbor, fileMap.get(fileNeighbor) || neighborFileMap.get(fileNeighbor));
                    }
                    else {
                        fileMap.put(fileNeighbor, false);
                    }
                }
                System.out.println("New file map: " + fileMap);
                node.setNodeFileMap(fileMap);

            }
            else {
                logger.info("Handle confirm message - neighborFileMap was null");
            }
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }

    }

    public void handleRequestMessage(ACLMessage msg) {
        String content = msg.getContent();
        System.out.println("Agent " + getLocalName() + " received REQUEST: " + content);

        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.CONFIRM);
        try {
            reply.setContentObject(fileMap);
        } catch (IOException e) {
            logger.info("ERROR SyncAgent, msg.setContentObject(fileMap): " + e);
        }
        send(reply);

    }

    private void lockFile(String fileName) {
        // Example of Distributed file locking logic
        int fileLocked = isFileLocked(fileName);
        switch(fileLocked) {
            case 1:
                System.out.println("File '" + fileName + "' is locked.");
            case 0 :
                System.out.println("File '" + fileName + "' not locked at the moment.");
            default:
                System.out.println("File was not found in the file map");
        }
    }

    private int isFileLocked(String fileName) {
        if (fileMap.containsKey(fileName)) {
            if (fileMap.get(fileName)) { // lock-value is true
                return 1;
            }
            else { // lock is false
                return 0;
            }
        }
        else {
            logger.info(fileName + "not found in the file map");
            return -1;
        }
    }


}