package origin.project.client.service;

import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.server.controller.NamingServerController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class MessageService {
    private static final String MULTICAST_GROUP = "230.0.0.0";
    private static final int PORT = 8888;
    private static final String hostnameServer = "localhost";
    private static final int portServer = 8080;
    private static final String namingServerUrl = "http://" + hostnameServer + ":" + portServer + "/naming-server";
    private Node node;
    private MulticastSocket socket;
    Logger logger = Logger.getLogger(NamingServerController.class.getName());


    public MessageService(Node node) throws IOException {
        this.node = node;

        socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        socket.joinGroup(group);

        new Thread(this::receiveMessage).start();
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

    public void receiveMessage() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                logger.info(message);

                if (Objects.equals(packet.getAddress().toString(), hostnameServer)) {
                    processUnicastMessage(message, packet.getAddress().getHostAddress());
                } else {
                    processMulticastMessage(message, packet.getAddress().getHostAddress());
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void processUnicastMessage(String message, String senderIPAddress) throws IOException {
        logger.info("Yessss");
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
        String url_sender = namingServerUrl + "/get-hash/" + senderName;
        int senderHash = Integer.parseInt(Objects.requireNonNull(getRequest(url_sender, "get sender hash")));

        // Calculate hash of this node's name and IP address (assuming these are set in the Node class)
        String url_current = namingServerUrl + "/get-hash/" + node.getNodeName();
        int currentHash = Integer.parseInt(Objects.requireNonNull(getRequest(url_current, "get current hash")));

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

    private String getRequest(String endpoint, String request) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // If the request successful (status code 200), we can read response.
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Reader reads the response from the input stream.
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // Builder is used to build the full response from the lines we read with the reader.
                StringBuilder response = new StringBuilder();
                // building the full response, including status messages, headers, ...
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                //
                return response.toString();
            } else {
                // If the request was not successful, handle the error accordingly
                System.out.println(request + "failed" + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (IOException e) {
            // Handling network-related errors
            e.printStackTrace();
        }
        return null;
    }

}
