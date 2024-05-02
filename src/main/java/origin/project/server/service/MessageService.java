package origin.project.server.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.dto.NodeRequest;
import origin.project.server.repository.NamingRepository;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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
}
