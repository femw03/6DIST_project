package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import origin.project.client.Node;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.model.naming.dto.NodeRequest;

import java.io.IOException;
import java.net.*;
import java.util.Optional;

@Service
public class ShutdownService {
    /*
     * Shutdown, tell (next hash ID + previous hash ID)
     * Delete Node from namingServer mapping
     *  */
    @Autowired
    Node node;

    private static final String namingServerUrl = "/naming-server";

    private int nextID;
    private int previousID;
    private int myID;
    private InetAddress IPnext;
    private InetAddress IPprevious;
    private static final int PORT = 8888;
    public void Shutdown(Node node) throws UnknownHostException {
        RestTemplate restTemplate = new RestTemplate();
        nextID = node.getNextID();
        previousID = node.getPreviousID();
        myID = node.getCurrentID();

        // next
        String URLnext = namingServerUrl + "/get-IP-by-hash/" + nextID;
        IPnext = restTemplate.getForObject(URLnext, InetAddress.class);

        // previous
        String URLprevious = namingServerUrl + "/get-IP-by-hash/" + previousID;
        IPprevious = restTemplate.getForObject(URLprevious, InetAddress.class);

        // sending
        sendID(IPnext,myID,previousID);
        sendID(IPprevious,myID,nextID);

        // remove mine
        String URLdelete = namingServerUrl + "/remove-node/" + new NodeRequest(node.getNodeName(),node.getIpAddress());
        restTemplate.delete(URLdelete);
    }

    private void sendID(InetAddress receiverIP, int ID, int targetID) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress receiverAddress = receiverIP;
            String responseMessage = ID + "," + targetID;
            byte[] buf = responseMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverAddress, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
