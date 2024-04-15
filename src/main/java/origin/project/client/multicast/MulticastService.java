package origin.project.client.multicast;

import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.service.HashService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@Service
public class MulticastService {
    private static final String MULTICAST_GROUP = "230.0.0.0";
    private static final int PORT = 8888;
    private static final String namingServerUrl = "/naming-server";
    private Node node;
    private MulticastSocket socket;
    public MulticastService(Node node) throws IOException {
        this.node = node;

        socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        socket.joinGroup(group);

        new Thread(this::receiveMulticastMessage).start();
    }

    public static void sendMulticastMessage(String nodeName, InetAddress IP) {
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            try (DatagramSocket socket = new DatagramSocket()) {
                String message = nodeName + "," + IP; // Combine name and IP address
                byte[] buf = message.getBytes();

                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMulticastMessage() {

        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                processMulticastMessage(message, packet.getAddress().getHostAddress());
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void processMulticastMessage(String multicastMessage, String senderIPAddress) throws IOException {
        // Extract sender's name and IP address from the message
        String[] parts = multicastMessage.split(",");

        if (parts.length != 2) {
            throw new IOException("Invalid multicast message format");
        }

        String senderName = parts[0];
        String senderIP = parts[1];

        // Calculate hash of node that sent multicast message
        int senderHash = HashService.calculateHashFromNamingServer(senderName, namingServerUrl);

        // Calculate hash of this node's name and IP address (assuming these are set in the Node class)
        int currentHash = HashService.calculateHashFromNamingServer(node.getName(), namingServerUrl);;

        // Update currentID, nextID, previousID based on the received multicast message
        if (currentHash < senderHash && senderHash < node.getNextID()) {
            node.setNextID(senderHash);
            // Send response to sender with currentID and nextID information
            sendResponse(senderIPAddress, node.getCurrentID(), node.getNextID());
        }

        if (node.getPreviousID() < senderHash && senderHash < currentHash) {
            node.setPreviousID(senderHash);
            // Send response to sender with currentID and previousID information
            sendResponse(senderIPAddress, node.getCurrentID(), node.getPreviousID());
        }
    }

    private void sendResponse(String receiverIP, int currentID, int targetID) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress receiverAddress = InetAddress.getByName(receiverIP);
            String responseMessage = currentID + "," + targetID;
            byte[] buf = responseMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverAddress, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
