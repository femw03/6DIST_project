package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
import java.net.*;


@Service
public class PingService {
    @Autowired
    private Node node;
    private int PORT;
    private String MULTICAST_GROUP;
    private int nextID;
    private int previousID;
    private int myID;
    private InetAddress IPnext;
    private InetAddress IPprevious;
    private static final String hostnameServer = "localhost";
    private static final int portServer = 8080;
    private static final String namingServerUrl = "http://" + hostnameServer + ":" + portServer + "/naming-server";
    private MulticastSocket socket;
    public PingService(Node node) throws IOException {
        this.node = node;
        this.PORT = node.getMulticastPort();
        this.MULTICAST_GROUP = node.getMulticastGroup();

        socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        socket.joinGroup(group);

        new Thread(this::Ping).start();
    }
    public void Ping() {
        try {
            nextID = node.getNextID();
            previousID = node.getPreviousID();
            myID = node.getCurrentID();

            // next
            String URLnext = namingServerUrl + "/get-IP-by-hash/" + nextID;
            IPnext = InetAddress.getByName(MessageService.getRequest(URLnext, "get next ip"));

            // previous
            String URLprevious = namingServerUrl + "/get-IP-by-hash/" + previousID;
            IPprevious = InetAddress.getByName(MessageService.getRequest(URLprevious, "get previous ip"));

            //send
            sendResponse(IPnext.toString());
            sendResponse(IPprevious.toString());
        }

        catch (IOException e){
            e.printStackTrace();
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
