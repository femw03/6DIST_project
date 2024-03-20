package origin.project.naming.service;

public class Hasher {
    public static int hashFileName(String fileName) {
        int max = 32768;
        int min = 0;
        int hash = (fileName.hashCode() % (max - min + 1)) + max;
        return hash % 32768;
    }
}