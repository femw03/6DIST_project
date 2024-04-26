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

    /*
    * code is idem as in shutdownservice.java
    * move to one file to manage code replication!!!
    */
    public void Failure(Node node, MessageService messageService) throws UnknownHostException {
        this.node = node;
        this.messageService = messageService;
        int nextID = node.getNextID();
        int previousID = node.getPreviousID();
        int myID = node.getCurrentID();

        // next
        String URLnext = node.getNamingServerUrl() + "/get-IP-by-hash/" + nextID;
        String IPnext = messageService.getRequest(URLnext, "get next ip");
        IPnext = IPnext.replace("\"", "");              // remove double quotes
        InetAddress IPnextInet =  InetAddress.getByName(IPnext);

        // previous
        String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + previousID;
        String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
        IPprevious = IPprevious.replace("\"", "");              // remove double quotes
        InetAddress IPpreviousInet =  InetAddress.getByName(IPprevious);

        // Sending
        messageService.sendMessage(IPnextInet,previousID,-1);
        messageService.sendMessage(IPpreviousInet,-1,nextID);

        // remove mine
        String URLdelete = node.getNamingServerUrl() + "/remove-node/";
        String nodeBody = "{\"name\" : \"" + node.getNodeName() + "\", \"ip\" : \"" + node.getIpAddress() + "\"}" ;
        messageService.deleteRequest(URLdelete, nodeBody, "removeNode");
    }
}
