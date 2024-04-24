package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;


//@Service
public class PingService {
    //@Autowired
    private MessageService messageService;
    //@Autowired
    private Node node;
    private int PORT;
    private String MULTICAST_GROUP;
    private InetAddress IPnext;
    private InetAddress IPprevious;
    private String namingServerUrl;
    private MulticastSocket socket;
    static Logger logger = Logger.getLogger(PingService.class.getName());
    public PingService(Node node) throws IOException {
        this.node = node;
        this.PORT = node.getMulticastPort();
        this.MULTICAST_GROUP = node.getMulticastGroup();
        this.namingServerUrl = node.getNamingServerUrl();

        socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        socket.joinGroup(group);

        new Thread(this::Ping).start();
    }
    public void Ping() {
        while (true) {
            try {
                int nextID = node.getNextID();
                int previousID = node.getPreviousID();
                int myID = node.getCurrentID();

                // ???
                if (nextID != -1 && previousID != -1 && myID != -1) {
                    // next
                    String URLnext = namingServerUrl + "/get-IP-by-hash/" + nextID;
                    IPnext = InetAddress.getByName(messageService.getRequest(URLnext, "get next ip"));

                    // previous
                    String URLprevious = namingServerUrl + "/get-IP-by-hash/" + previousID;
                    IPprevious = InetAddress.getByName(messageService.getRequest(URLprevious, "get previous ip"));

                    //send
                    sendResponse(IPnext.toString());
                    sendResponse(IPprevious.toString());
                } else {
                    logger.info("nextID: " + nextID + " previousID: " + previousID + " myID: " + myID);
                    // Find way to make him wait on response from server!!!
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(String receiverIP) throws UnknownHostException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress receiverAddress = InetAddress.getByName(receiverIP);
            String responseMessage = "hello";
            byte[] buf = responseMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverAddress, PORT);
            socket.send(packet);
        } catch (IOException e) {
            try {
                new FailureService().Failure(node); //???
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
        }
    }
}
