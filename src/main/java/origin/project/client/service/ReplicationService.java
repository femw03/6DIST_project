package origin.project.client.service;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.server.controller.NamingServerController;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class ReplicationService {
    @Autowired
    Node node;

    @Autowired
    MessageService messageService;

    @Value("${localfiles.path}")
    String FOLDER_PATH;

    Path baseFolder;

    File localFileFolder;

    ArrayList<String> fileNames;

    Logger logger = Logger.getLogger(NamingServerController.class.getName());

    String replicationBaseUrl;

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (!node.isDiscoveryFinished()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                actualInit();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }

    public void actualInit() throws UnknownHostException{
        // allows for local testing
//        System.out.println(node.getNamingServerIp());
//        if (node.getNamingServerIp() == null) {
//            node.setNamingServerIp(InetAddress.getByName("127.0.0.1"));
//        }

        localFileFolder = new File(FOLDER_PATH);
        baseFolder = Paths.get(FOLDER_PATH);
        fileNames = new ArrayList<>();

        replicationBaseUrl = "http:/"+node.getNamingServerIp()+":"+node.getNamingServerPort()+"/replication";

        // verify local files and send hash-values to naming server
        startUp();

        // start update thread
    }


    // Starting
    //  - Verify local files
    //  - Report hash-values to naming server
    public void startUp() {
        // get current files.
        scanFolder(localFileFolder, fileNames);

        logger.info("Found following files: " + fileNames);

        Map<String, Integer> hashedFileNames = new HashMap<>();

        // hash fileNames
        for (String fileName : fileNames) {
            hashedFileNames.put(fileName, hashingFunction(fileName));
        }

        // Convert array to JSON
        String hashedFileNamesJSON = new Gson().toJson(hashedFileNames);

        // report hashedFileNames to Naming Server
        logger.info("hashed FileNames JSON" + hashedFileNamesJSON);

        String replicationEndpoint = replicationBaseUrl + "/initial-list";
        logger.info("Endpoint for the initial list post-request: " + replicationEndpoint);

        String replicationMap = messageService.postRequest(replicationEndpoint, hashedFileNamesJSON,"Initial local files list");
        logger.info("Replication map returned by server : " + replicationMap);




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
