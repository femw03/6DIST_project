package origin.project.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import origin.project.server.controller.NamingServerController;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.repository.NamingRepository;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class NamingService {
    Logger logger = Logger.getLogger(NamingServerController.class.getName());

    @Autowired
    NamingRepository namingRepository;

    public int hashingFunction(String name) {
        // Hash code given in ppt, but nodes with almost same name got same hash
        /*double max = 2147483647.0;
        double min = -2147483647.0;
        double hash = (name.hashCode() + max) * ( 32768.0 / (max + Math.abs(min)));
        return (int)hash;*/

        int hash = name.hashCode();  // Use Java's hashCode method to get an initial hash code

        // Ensure the hash code falls within the range [0, 32767] (inclusive)
        hash = Math.abs(hash) % 32768;

        return hash;
    }

    public NamingEntry findNearestNodeId(Integer fileHash, ArrayList<NamingEntry> nodesWithHashSmallerThanFile) {
        NamingEntry bestNode = nodesWithHashSmallerThanFile.get(0);
//        System.out.println("firstBestNode in findNearest :" + bestNode.getIP() + " " + bestNode.getHash());
        int minDifference = fileHash - bestNode.getHash();
//        System.out.println("minDifference firstBestNode :" + minDifference);

        for (NamingEntry node : nodesWithHashSmallerThanFile) {
//            System.out.println("nextNode in findNearest :" + node.getIP() + " " + node.getHash());
            int difference = fileHash - node.getHash();
//            System.out.println("nextDifference in findNearest : " + difference);
            if (difference < minDifference) {
                minDifference = difference;
                bestNode = node;
            }
        }
        return bestNode;
    }

    public InetAddress findOwner(int fileHash) {
        ArrayList<NamingEntry> nodesWithHashSmallerThanFile = (ArrayList<NamingEntry>) namingRepository.findByHashLessThan(fileHash);

        if (nodesWithHashSmallerThanFile.isEmpty()) {
            // If no nodes with hash smaller than file hash, find the node with the biggest hash
            Optional<NamingEntry> ownerNodeOptional = namingRepository.findEntryWithLargestHash();
            if (ownerNodeOptional.isPresent()) {
                NamingEntry ownerNode = ownerNodeOptional.get();
                return ownerNode.getIP();
            }
            else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No suitable owner found.");
            }
        }
        else {
            // Find the node with the smallest difference between its hash and the file hash
            NamingEntry ownerNode = findNearestNodeId(fileHash,nodesWithHashSmallerThanFile);
            return ownerNode.getIP();
        }
    }
}