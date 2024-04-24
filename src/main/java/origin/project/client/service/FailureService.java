package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class FailureService {
    @Autowired
    private MessageService messageService;
    @Autowired
    Node node;
    private static final String hostnameServer = "localhost";
    private static final int portServer = 8080;
    private static final String namingServerUrl = "http://" + hostnameServer + ":" + portServer + "/naming-server";
    private int nextID;
    private int previousID;
    private int myID;
    private InetAddress IPnext;
    private InetAddress IPprevious;
    private static final int PORT = 8888;

    /*
    * code is idem as in shutdownservice.java
    * move to one file to manage code replication!!!
    */
    public void Failure(Node node) throws UnknownHostException {
        nextID = node.getNextID();
        previousID = node.getPreviousID();
        myID = node.getCurrentID();

        // next
        String URLnext = namingServerUrl + "/get-IP-by-hash/" + nextID;
        IPnext = InetAddress.getByName(messageService.getRequest(URLnext, "get next ip"));

        // previous
        String URLprevious = namingServerUrl + "/get-IP-by-hash/" + previousID;
        IPprevious = InetAddress.getByName(messageService.getRequest(URLprevious, "get previous ip"));

        // sending
        sendID(IPnext,myID,previousID);
        sendID(IPprevious,myID,nextID);

        // remove mine
        String URLdelete = namingServerUrl + "/remove-node/";
        String nodeBody = "{\"name\" : \"" + node.getNodeName() + "\", \"ip\" : \"" + node.getIpAddress() + "\"}" ;
        messageService.deleteRequest(URLdelete, nodeBody, "removeNode");
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
