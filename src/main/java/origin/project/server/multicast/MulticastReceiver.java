package origin.project.server.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.model.naming.dto.NodeRequest;
import origin.project.server.repository.NamingRepository;
import origin.project.server.service.JsonService;
import origin.project.server.service.NamingService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@Service
public class MulticastReceiver {
    @Autowired
    private NamingService namingService;
    @Autowired
    private JsonService jsonService;
    @Autowired
    private NamingRepository namingRepository;
    @Autowired
    private NamingServerController namingServerController;

    private static final String FILE_PATH = "src/main/resources/nodes.json";

    private static final int PORT = 8888;
    private static final String MULTICAST_GROUP = "230.0.0.0";

    private MulticastSocket socket;

    public MulticastReceiver(NamingService namingService, JsonService jsonService, NamingRepository namingRepository, NamingServerController namingServerController) throws IOException {
        this.namingService = namingService;
        this.jsonService = jsonService;
        this.namingRepository = namingRepository;
        this.namingServerController = namingServerController;

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
                String ipAddress = parts[1];

                namingServerController.addNode(new NodeRequest(nodeName, ipAddress));

            /*
            // Calculate hash based on node name
            int hash = namingService.hashingFunction(nodeName);

            // Add hash and IP address to map data structure
            NamingEntry namingEntry = new NamingEntry(hash, ipAddress);
            jsonService.addEntryToJsonFile(FILE_PATH, namingEntry);
            namingRepository.save(namingEntry);

            */

                // Respond to new node with number of existing nodes
                int existingNodesCount = (int) namingRepository.count();

                System.out.println("Multicast message handled successfully. Existing nodes count: " + existingNodesCount);

                //namingServerController.respondToMulticast(existingNodesCount);

                sendResponse(String.valueOf(existingNodesCount), ipAddress);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(String message, String receiverIP) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress receiverAddress = InetAddress.getByName(receiverIP);
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverAddress, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
