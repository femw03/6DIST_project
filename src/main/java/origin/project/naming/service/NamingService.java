package origin.project.naming.service;

import java.util.ArrayList;
import java.util.Map;

public class NamingService {
    public static int hashingFunction(String name) {
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

    public static int findNearestNodeId(Integer fileHash, ArrayList<Integer> nodesWithHashSmallerThanFile) {
        int minDifference = Integer.MAX_VALUE;
        int nearestNodeId = -1;

        for (Integer nodeId : nodesWithHashSmallerThanFile) {
            int difference = fileHash - nodeId;
            if (difference < minDifference) {
                minDifference = difference;
                nearestNodeId = nodeId;
            }
        }
        return nearestNodeId;
    }

    public static int findBiggestNodeHash(Map<Integer, String> nodeMap) {
        int maxHash = Integer.MIN_VALUE;
        int biggestNodeHashId = -1;

        for (Integer nodeId : nodeMap.keySet()) {
            if (nodeId > maxHash) {
                maxHash = nodeId;
                biggestNodeHashId = nodeId;
            }
        }
        return biggestNodeHashId;
    }
}