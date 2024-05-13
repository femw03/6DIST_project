package origin.project.client.agents;


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
