package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
import java.net.*;
import java.util.Objects;
import java.util.logging.Logger;


@Service
public class PingService {
    @Autowired
    private Node node;
    @Autowired
    private MessageService messageService;
    @Autowired
    private FailureService failureService;
    private int PORT;
    private String MULTICAST_GROUP;
    static Logger logger = Logger.getLogger(PingService.class.getName());
    int i;

    public PingService(Node node) {
        this.node = node;
        this.PORT = node.getMulticastPort();
        this.MULTICAST_GROUP = node.getMulticastGroup();
        this.i=0;

        new Thread(this::Ping).start();
    }

    public void Ping() {
        int temp = node.getExistingNodes(); // test
        while(true){
            try {
                Thread.sleep(2500); // 2.5 seconds
                int nextID = node.getNextID();
                int previousID = node.getPreviousID();
                //logger.info("nodes: "+node.getExistingNodes());

                if (node.getExistingNodes() > 1 && node.isPingEnable()) {

                    // next
                    if (nextID != -1 && node.getExistingNodes() > 1) { //added check for existing node count. Reason, seems to get into big if statement sometimes when he shouldn't
                        String URLnext = node.getNamingServerUrl() + "/get-IP-by-hash/" + nextID;
                        String IPnext = messageService.getRequest(URLnext, "get next ip");
                        IPnext = IPnext.replace("\"", "");              // remove double quotes
                        InetAddress IPnextInet = InetAddress.getByName(IPnext);
                        //PING
                        pingNode(IPnextInet);
                    }

                    // previous
                    if (previousID != -1 && node.getExistingNodes() > 1) {
                        String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + previousID;
                        String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
                        IPprevious = IPprevious.replace("\"", "");              // remove double quotes
                        InetAddress IPpreviousInet = InetAddress.getByName(IPprevious);
                        //PING
                        pingNode(IPpreviousInet);
                    }

                }
            } catch (InterruptedException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void pingNode(InetAddress receiverIP) throws UnknownHostException, InterruptedException {
        try (Socket socket = new Socket()) {
            //Thread.sleep(2500); // 2.5 seconds
            logger.info("Sending PING to "+receiverIP.getHostAddress());
            socket.connect(new InetSocketAddress(receiverIP.getHostName(), node.getNodePort()), 30);
        } catch (IOException e) {
            // Connection failed, handle the exception or throw it further
            logger.info("Failed to connect to node: "+ receiverIP.getHostAddress());
            node.setPingEnable(false);
            node.setExistingNodes(node.getExistingNodes()-1);           // Update active node count
            failureService.Failure(receiverIP.getHostAddress());
        }
    }

}
