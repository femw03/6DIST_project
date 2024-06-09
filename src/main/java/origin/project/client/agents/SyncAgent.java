package origin.project.client.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import origin.project.client.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SyncAgent extends Agent {

    private Node node;

    private ConcurrentHashMap<String, Boolean> fileMap;

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

            ArrayList<String> initialFileList = castObjectToArrayListOfString(args[1]);


            fileMap = new ConcurrentHashMap<>();
            if (initialFileList.isEmpty()) {
                logger.info("initial file list has size 0");
            }
            else {
                for (String element : initialFileList) {
                    fileMap.put(element, false);
                }
            }


        }
        else {
            System.err.println("Error during parameter transfer");
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

    public ArrayList<String> castObjectToArrayListOfString(Object obj) {
        ArrayList<String> arrayList = new ArrayList<>();
        // Solve unchecked cast of Object to ArrayList<String>
        if (obj instanceof ArrayList<?> templist) {
            for (Object e : templist) {
                if (e instanceof String) {
                    arrayList.add((String) e);
                }

            }
        }
        return arrayList;
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

    public void handleConfirmMessage(ACLMessage msg) {
        //String content = msg.getContent();
//        System.out.println("Agent " + getLocalName() + " received CONFIRM: " + content);

        Object contentObject;
        try {
            contentObject = msg.getContentObject();
            ConcurrentHashMap<String, Boolean> newFileLog = castObjectToMapStringBoolean(contentObject);
            if (newFileLog != null) {
//
//                fileMap.putAll(newFileLog);
                System.out.println(newFileLog);

            }
            else {
                boolean justBecauseIHaveTo = false;
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
        this.send(reply);

    }

    public void handleFailedMessage(ACLMessage msg) {

    }

}





/*package origin.project.client.agents;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.util.ArrayList;
import java.util.List;

@Service
public class SyncAgent extends Agent {
    @Autowired
    private Node node;

    private List<String> fileList;
    private String nextNodeAgent;

    protected void setup() {
        // Initialize agent setup here
        // Get the list of files this node owns
        fileList = new ArrayList<>();
        nextNodeAgent = "SyncAgent"+node.getNextID();

        // Schedule periodic task for synchronization: updating file list and locking files
        addBehaviour(new SyncBehavior());

    }

    public class SyncBehavior extends TickerBehaviour {

        public SyncBehavior() {
            // Specify the period for synchronization (e.g., every 10 seconds)
            super(null, 5000); // Adjust the time interval as needed
        }

        @Override
        protected void onTick() {
            // Implement synchronization logic here
            synchronizeFiles();
        }

        private void synchronizeFiles() {
            // Logic to synchronize files with the next node
            // You can implement communication with neighboring nodes here
            // Example: Ask the next node's Sync Agent for its file list
            // Update this node's file list based on the received information
            // Implement distributed file locking logic if needed
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID(nextNodeAgent, AID.ISLOCALNAME));
            msg.setContent("Send me your file list");
            send(msg);

            ACLMessage reply = receive();
            if (reply != null) {
                String fileListFromNextNode = reply.getContent();
                // Update this node's file list based on the received file list from the next node
                updateFileList(fileListFromNextNode);
            }
        }
    }

    private void updateFileList(String fileListFromNextNode) {
        // Logic to update this node's file list based on the received file list from the next node
        System.out.println("Updating file list based on received information from next node: " + fileListFromNextNode);
    }

    private void lockFile(String fileName) {
        // Distributed file locking logic
        // Example logic (replace with actual implementation):
        boolean fileLocked = checkIfFileCanBeLocked(fileName);
        if (fileLocked) {
            System.out.println("File '" + fileName + "' locked successfully.");
            // Perform operations on the locked file
            // Example: Download the file or allow modifications
        } else {
            System.out.println("File '" + fileName + "' cannot be locked at the moment.");
            // Handle the case where the file cannot be locked (e.g., display an error message)
        }
    }

    private boolean checkIfFileCanBeLocked(String fileName) {
        // Logic to check if the file can be locked
        // Example: Check if the file is not already locked by another node
        // You may need to maintain a list of locked files and their states across nodes
        // For simplicity, assume the file can always be locked in this example
        return true;
    }

    // Agent cleanup
    protected void takeDown() {
        // Clean up resources if needed
    }



}
*/