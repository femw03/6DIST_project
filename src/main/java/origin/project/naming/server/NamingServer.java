package origin.project.naming.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import origin.project.naming.node.Node;
import origin.project.naming.repository.DatabaseLoader;
import origin.project.naming.repository.NamingEntry;
import origin.project.naming.repository.NodeRepository;
import origin.project.naming.service.NamingService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NamingServer {
    private static final String FILE_PATH = "nodes.json";

    @Autowired
    private NodeRepository nodeRepository;

    private Map<Integer, String> nodeMap;       // Map to store couples of (nodeID, IP address)

    public NamingServer() {
        nodeMap = new HashMap<>();
        //loadMapFromFile();
    }

    // Load nodes from file on initialization
    private void loadMapFromFile() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                nodeMap = Files.lines(file.toPath())
                        .map(line -> line.split(","))
                        .collect(Collectors.toMap(parts -> Integer.parseInt(parts[0]), parts -> parts[1]));
            }
        } catch (IOException e) {
            System.err.println("Error loading nodes from file: " + e.getMessage());
        }
    }

    // Save nodes to file
    private void saveMapToFile() {
        try {
            File file = new File(FILE_PATH);
            Files.write(file.toPath(), nodeMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "," + entry.getValue() + "\n")
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            System.err.println("Error saving nodes to file: " + e.getMessage());
        }
    }

    // Method to find out where a specific file is located
    public String getFileLocation(String fileName) {
        int fileHash = NamingService.hashingFunction(fileName);
        ArrayList<Integer> nodesWithHashSmallerThanFile = new ArrayList<>();
        int ownerNodeId = -1;

        if (nodeMap.isEmpty()) {
            throw new IllegalStateException("No nodes available in the system.");
        }

        for (Integer nodeId : nodeMap.keySet()) {
            if (nodeId < fileHash) {
                nodesWithHashSmallerThanFile.add(nodeId);
            }
        }

        if (nodesWithHashSmallerThanFile.isEmpty()) {
            // If no nodes with hash smaller than file hash, find the node with the biggest hash
            ownerNodeId = NamingService.findBiggestNodeHash(nodeMap);

        } else {
            // Find the node with the smallest difference between its hash and the file hash
            ownerNodeId = NamingService.findNearestNodeId(fileHash,nodesWithHashSmallerThanFile);
        }

        return nodeMap.get(ownerNodeId);
    }

    public void addNode(String nodeName, String ipAddress) {
        int nodeId = NamingService.hashingFunction(nodeName);

        Optional<NamingEntry> existingNodeIp = nodeRepository.findByIpAddress(ipAddress);
        Optional<NamingEntry> existingNodeId = nodeRepository.findById(nodeId);
        if (existingNodeIp.isPresent()) {
            throw new IllegalArgumentException("ERROR: Node with ip-address '" + ipAddress + "' already exists.");
        }
        if (existingNodeId.isPresent()) {
            throw new IllegalArgumentException("ERROR: Node with name '" + nodeId + "' already exists.");
        }

        Node newNode = new Node(nodeName, ipAddress);
        nodeRepository.save(new NamingEntry(newNode));

        // Update nodeMap and save to file
        nodeMap.put(nodeId, ipAddress);
        DatabaseLoader.updateEntriesToJsonFile(nodeRepository);
    }

    public void removeNode(String ipAddress) {
        Optional<NamingEntry> entry = nodeRepository.findByIpAddress(ipAddress);
        if (entry.isPresent()) {
            int nodeId = entry.get().getNodeId();
            nodeRepository.delete(entry.get());
            nodeMap.remove(nodeId);
            DatabaseLoader.updateEntriesToJsonFile(nodeRepository);
        } else {
            throw new IllegalArgumentException("ERROR: Node with ip-address '" + ipAddress + "' does not exist.");
        }
    }

    public NamingEntry getNodeByIp(String ipAddress) {
        return nodeRepository.findByIpAddress(ipAddress)
                .orElseThrow(() -> new IllegalArgumentException("ERROR: Node with ip-address '" + ipAddress + "' does not exist."));
    }

    public Iterable<NamingEntry> getAllNodes() {
        return nodeRepository.findAll();
    }

    public void deleteAll() {
        if (nodeRepository.count() == 0) {
            throw new IllegalArgumentException("NodeRepository is already empty. No action taken.");
        } else {
            nodeRepository.deleteAll();
            nodeMap = new HashMap<>();
            DatabaseLoader.updateEntriesToJsonFile(nodeRepository);
        }
    }

}