package origin.project.client.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.server.controller.NamingServerController;

import java.io.File;
import java.lang.reflect.Type;
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

    String FOLDER_PATH;

    Path baseFolder;

    File localFileFolder;

    ArrayList<String> fileNames;

    Logger logger = Logger.getLogger(NamingServerController.class.getName());

    String replicationBaseUrl;

    boolean updateThreadRunning = false;

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

        FOLDER_PATH = node.getFolderPath();
        localFileFolder = new File(FOLDER_PATH);
        baseFolder = Paths.get(FOLDER_PATH);
        fileNames = new ArrayList<>();

        replicationBaseUrl = "http:/"+node.getNamingServerIp()+":"+node.getNamingServerPort()+"/replication";

        // verify local files and send hash-values to naming server
        startUp();

        // start update thread
        updateThreadRunning = true;
        new Thread(this::updateThread);

    }


    // Starting
    //  - Verify local files
    //  - Report hash-values to naming server
    //  - Transfer files to the owner-nodes.
    public void startUp() throws UnknownHostException {
        // get current files.
        scanFolder(localFileFolder, fileNames);

        logger.info("Found following files: " + fileNames);

        if (fileNames.isEmpty()) {
            logger.info("Breaking replication start-up because no local files were found");
            return;
        }

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

        String replicationMapJSON = messageService.postRequest(replicationEndpoint, hashedFileNamesJSON,"Initial local files list");
        logger.info("Replication map returned by server : " + replicationMapJSON);

        // local-files are present so replication map cannot be empty.
        if (replicationMapJSON == null || replicationMapJSON.trim().isBlank()) {
            throw new RuntimeException("Server returned invalid replication map.");
        }

        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
        Map<String, String> replicationMap = new Gson().fromJson(replicationMapJSON, type);

        // send files to owner-node
        for (String fileName : replicationMap.keySet()) {
            InetAddress targetIP = InetAddress.getByName(replicationMap.get(fileName));
            File file = new File(fileName);

            System.out.println(targetIP + fileName);

            // skip files belonging to current
            // use file-transfer-service to pass file.
        }

    }

    public void updateThread() {
        while(updateThreadRunning) {
            // check updates

            // report updates

            // every 10 seconds
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
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
