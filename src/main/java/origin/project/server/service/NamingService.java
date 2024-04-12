package origin.project.server.service;

import org.springframework.stereotype.Service;
import origin.project.server.model.naming.NamingEntry;

import java.util.ArrayList;

@Service
public class NamingService {
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
}