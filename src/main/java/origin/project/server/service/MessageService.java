package origin.project.server.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.server.Server;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.dto.NodeRequest;
import origin.project.server.repository.NamingRepository;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Logger;

@Service
public class MessageService {
    @Autowired
    private NamingRepository namingRepository;
    @Autowired
    private NamingServerController namingServerController;
    @Autowired
    private Server server;

    private int PORT;
    private String MULTICAST_GROUP ;
    private MulticastSocket socket;
    Logger logger = Logger.getLogger(MessageService.class.getName());


    public MessageService(NamingRepository namingRepository, NamingServerController namingServerController) throws IOException {
        this.namingRepository = namingRepository;
        this.namingServerController = namingServerController;
    }

    @PostConstruct
    public void init() throws IOException {
        PORT = namingServerController.getMulticastPort();
        MULTICAST_GROUP = namingServerController.getMulticastGroup();

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

                if (parts.length != 2) {
                    throw new IOException("Invalid multicast message format");
                }

                String nodeName = parts[0];
                String ipAddressString = parts[1];
                if (ipAddressString.startsWith("/")) {
                    // Extract the IP address without the leading slash
                    ipAddressString = ipAddressString.substring(1);
                }
                InetAddress ipAddress = InetAddress.getByName(ipAddressString);

                namingServerController.addNode(new NodeRequest(nodeName, ipAddress));

                // Respond to new node with number of existing nodes
                int existingNodesCount = (int) namingRepository.count();

                logger.info("Multicast message handled successfully. Existing nodes count: " + existingNodesCount);

                sendResponse(String.valueOf(existingNodesCount), ipAddress);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(String message, InetAddress receiverIP) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverIP, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
