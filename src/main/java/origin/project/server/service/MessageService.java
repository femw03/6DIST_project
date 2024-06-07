package origin.project.server.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.dto.NodeRequest;
import origin.project.server.repository.NamingRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class MessageService {
    @Autowired
    private NamingRepository namingRepository;
    @Autowired
    private NamingService namingService;
    @Autowired
    private NamingServerController namingServerController;

    private int PORT;
    private String MULTICAST_GROUP ;
    private MulticastSocket socket;
    Logger logger = Logger.getLogger(MessageService.class.getName());


    public MessageService(NamingRepository namingRepository, NamingServerController namingServerController, NamingService namingService) {
        this.namingRepository = namingRepository;
        this.namingServerController = namingServerController;
        this.namingService = namingService;
    }

    @PostConstruct
    public void init() throws IOException {
        PORT = namingServerController.getMulticastPort();
        MULTICAST_GROUP = namingServerController.getMulticastGroup();
        System.out.println(PORT);

        socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        socket.joinGroup(group);

        new Thread(this::receiveMulticastMessage).start();
    }

    public void receiveMulticastMessage() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());

                // Parse multicast message and extract node name, IP address
                String[] parts = message.split(",");

                if (Objects.equals(parts[0], "newNode")) {

                    if (parts.length != 3) {
                        throw new IOException("Invalid multicast message format");
                    }

                    String nodeName = parts[1];
                    String ipAddressString = parts[2];
                    if (ipAddressString.startsWith("/")) {
                        // Extract the IP address without the leading slash
                        ipAddressString = ipAddressString.substring(1);
                    }
                    InetAddress ipAddress = InetAddress.getByName(ipAddressString);

                    namingServerController.addNode(new NodeRequest(nodeName, ipAddress));

                    // Respond to new node with number of existing nodes
                    int existingNodesCount = (int) namingRepository.count();

                    logger.info("Multicast message handled successfully. Existing nodes count: " + existingNodesCount);

                    // Calculate hash of nodeName
                    int nodeHash = namingService.hashingFunction(nodeName);

                    sendResponse(existingNodesCount, nodeHash, ipAddress);
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(int existingNodesCount, int hash, InetAddress receiverIP) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String message = "namingServer," + existingNodesCount + "," + hash;
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverIP, PORT);
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
}
