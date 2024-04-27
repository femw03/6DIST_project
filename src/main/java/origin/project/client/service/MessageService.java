package origin.project.client.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class MessageService {
    @Autowired
    private Node node;

    private static int PORT;
    private static String MULTICAST_GROUP;
    private MulticastSocket socket;
    Logger logger = Logger.getLogger(MessageService.class.getName());


    public MessageService(Node node) {
        this.node = node;
    }

    @PostConstruct
    public void init() throws IOException {
        PORT = node.getMulticastPort();
        MULTICAST_GROUP = node.getMulticastGroup();

        socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        socket.joinGroup(group);

        new Thread(this::receiveMessage).start();
    }

    public void sendMulticastMessage(String nodeName, InetAddress IP) {
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            try (DatagramSocket socket = new DatagramSocket()) {
                String message = "newNode," + nodeName + "," + IP; // Combine name and IP address
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

                if (!Objects.equals(packet.getAddress().toString(), node.getIpAddress().toString())) {
                    processMessage(message, packet.getAddress());
                }

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message, InetAddress senderIPAddress) throws IOException {
        String[] parts = message.split(",");
        if (parts[0].equals("newNode")) {
            // Process multicast message
            logger.info("Received multicast");
            processMulticastMessage(message, senderIPAddress);
        } else {
            // Process unicast message
            logger.info("Received unicast");
            processUnicastMessage(message,senderIPAddress);
        }
    }

    private void processUnicastMessage(String message, InetAddress senderIPAddress) throws IOException {
        String[] parts = message.split(",");
        if (parts[0].equals("namingServer")) {
            logger.info("Processing unicast from naming server with IP address "+senderIPAddress.toString());
            node.setNamingServerIp(senderIPAddress);
            node.setNamingServerUrl("http:/"+node.getNamingServerIp()+":"+node.getNamingServerPort()+"/naming-server");

            if (parts.length != 3) {
                throw new IOException("Invalid multicast message format");
            }

            int existingNodes = Integer.parseInt(parts[1]);
            int currentID = Integer.parseInt(parts[2]);
            node.setCurrentID(currentID);

            if (existingNodes <= 1) {
                node.setPreviousID(node.getCurrentID());
                node.setNextID(node.getCurrentID());
                logger.info("Previous ID: " + node.getPreviousID());
                logger.info("Current ID: " + node.getCurrentID());
                logger.info("Next ID: " + node.getNextID());
            }
            node.setExistingNodes(existingNodes);
            logger.info("Existing nodes: "+node.getExistingNodes());

        } else {
            logger.info("Processing unicast from other node with IP address "+senderIPAddress.toString());
            if (parts.length != 2) {
                throw new IOException("Invalid multicast message format");
            }

            int previousID = Integer.parseInt(parts[0]);
            int nextID = Integer.parseInt(parts[1]);

            if (previousID == -1) {
                node.setNextID(nextID);
            } else if (nextID == -1) {
                node.setPreviousID(previousID);
            } else {
                node.setPreviousID(previousID);
                node.setNextID(nextID);
            }

            logger.info("Previous ID: " + node.getPreviousID());
            logger.info("Current ID: " + node.getCurrentID());
            logger.info("Next ID: " + node.getNextID());
        }

    }

    private void processMulticastMessage(String multicastMessage, InetAddress senderIPAddress) throws IOException {
        logger.info("Processing multicast from other node with IP address "+senderIPAddress.toString());
        // Extract sender's name and IP address from the message
        String[] parts = multicastMessage.split(",");

        if (parts.length != 3) {
            throw new IOException("Invalid multicast message format");
        }

        node.setExistingNodes(node.getExistingNodes()+1);
        logger.info("Existing nodes: "+node.getExistingNodes());

        // parts[0] = "newNode"
        String senderName = parts[1];
        String senderIP = parts[2];

        // Calculate hash of node that sent multicast message
        String url_sender = node.getNamingServerUrl() + "/get-hash/" + senderName;
        int senderHash = Integer.parseInt(Objects.requireNonNull(getRequest(url_sender, "get sender hash")));

        // Calculate hash of this node's name and IP address (assuming these are set in the Node class)
        String url_current = node.getNamingServerUrl() + "/get-hash/" + node.getNodeName();
        int currentHash = Integer.parseInt(Objects.requireNonNull(getRequest(url_current, "get current hash")));


        // Update currentID, nextID, previousID based on the received multicast message
        if (currentHash < senderHash && senderHash <= node.getNextID()) {
            // Send response to sender with currentID and nextID information
            sendMessage(senderIPAddress, node.getCurrentID(), node.getNextID());
            node.setNextID(senderHash);
        }

        else if (node.getPreviousID() <= senderHash && senderHash < currentHash) {
            // Send response to sender with currentID and previousID information
            sendMessage(senderIPAddress, node.getPreviousID(), node.getCurrentID());
            node.setPreviousID(senderHash);
        }

        else if (node.getPreviousID() == node.getNextID() && node.getPreviousID() == node.getCurrentID()) {     // only 1 node in network
            // Send response to sender with currentID information
            sendMessage(senderIPAddress, node.getCurrentID(), node.getCurrentID());
            node.setPreviousID(senderHash);
            node.setNextID(senderHash);
        }

        else if (node.getCurrentID() < node.getPreviousID()) {
            // Send response to sender with currentID information
            sendMessage(senderIPAddress, node.getPreviousID(), node.getCurrentID());
            node.setPreviousID(senderHash);
        }

        else if (node.getCurrentID() > node.getNextID()) {
            // Send response to sender with currentID information
            sendMessage(senderIPAddress, node.getCurrentID(), node.getNextID());
            node.setNextID(senderHash);
        }

        logger.info("Previous ID: " + node.getPreviousID());
        logger.info("Current ID: " + node.getCurrentID());
        logger.info("Next ID: " + node.getNextID());
    }

    public void sendMessage(InetAddress receiverIP, int currentID, int targetID) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String responseMessage = currentID + "," + targetID;
            byte[] buf = responseMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverIP, PORT);
            logger.info("Sending message to "+receiverIP);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRequest(String endpoint, String request) {
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

    public String deleteRequest(String endpoint, String requestbody, String request) {

        try {
            String output;

            URL url = new URL(endpoint);
//            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Try writing the email to JSON
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                byte[] requestBody = requestbody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(requestBody, 0, requestBody.length);
            }

            // If connection is successful, we can read the response
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // reader
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // response
                StringBuilder response = new StringBuilder();
                String line;
                // build response = adding status code, status message, headers, ...
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                output = response.toString();
            }
            else {
                // If the request was not successful, handle the error accordingly
                output = "Failed to " + request + ". HTTP Error: " + connection.getResponseCode();
            }
            connection.disconnect();
            return output;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String postRequest(String endpoint, String requestbody, String request) {

        try {
            String output;

            URL url = new URL(endpoint);
//            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Try writing the email to JSON
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                byte[] requestBody = requestbody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(requestBody, 0, requestBody.length);
            }

            // If connection is successful, we can read the response
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // reader
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // response
                StringBuilder response = new StringBuilder();
                String line;
                // build response = adding status code, status message, headers, ...
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                output = response.toString();
            }
            else {
                // If the request was not successful, handle the error accordingly
                output = "Failed to " + request + ". HTTP Error: " + connection.getResponseCode();
            }
            connection.disconnect();
            return output;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
