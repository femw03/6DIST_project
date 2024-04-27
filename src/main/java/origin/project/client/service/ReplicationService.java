package origin.project.client.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReplicationService {

    @Value("${local.files.path}")
    String FOLDER_PATH;

    Path baseFolder;

    File localFileFolder;

    ArrayList<String> fileNames;

    @PostConstruct
    public void init() {
        localFileFolder = new File(FOLDER_PATH);
        baseFolder = Paths.get(FOLDER_PATH);
        fileNames = new ArrayList<>();

    }


    // Starting
    //  - Verify local files
    //  - Report hash-values to naming server
    public void startUp() {
        // get current files.
        scanFolder(localFileFolder, fileNames);

        ArrayList<Integer> hashedFileNames = new ArrayList<>();

        // hash fileNames
        for (String fileName : fileNames) {
            hashedFileNames.add(hashingFunction(fileName));
        }

        // report hashedFileNames to Naming Server

    }

    /**
     * Find which files were added or deleted.
     * @return map of fileName and int which indicates new (0) or deleted (1)
     */
    public Map<String, Integer> findUpdates() {
        ArrayList<String> newFileList = new ArrayList<>();

        Map<String, Integer> updatedFilesMap = new HashMap<>();

        // get current files
        scanFolder(localFileFolder, newFileList);

        // added files = files in newList but not in saved list
        for (String fileName : newFileList) {
            if (!fileNames.contains(fileName)) {
                updatedFilesMap.put(fileName, 0);
            }
        }

        // deleted files = files in saved list but not in newList
        for (String fileName : fileNames) {
            if (!newFileList.contains(fileName)) {
                updatedFilesMap.put(fileName, 0);
            }
        }

        return updatedFilesMap;
    }




    public void scanFolder(File folder, ArrayList<String> fileNames) {
        // files in the folder
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanFolder(file, fileNames);
                } else {

                    Path target = Paths.get(file.getPath());
                    Path relativePath = baseFolder.relativize(target);

                    fileNames.add(relativePath.toString());
                }
            }
        }
    }


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


    public void replication() {

    }
}
