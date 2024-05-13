package origin.project.client.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
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
import java.util.logging.Logger;

@Service
public class ReplicationService {
    @Autowired
    Node node;
    @Autowired
    MessageService messageService;

    String FOLDER_PATH;

    @Autowired
    FileService fileService;
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

        // Convert array to JSON
        String hashedFileNamesJSON = new Gson().toJson(fileNames);

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

        Gson gson = new Gson();
        FileTransfer fileTransfer;
        // send files to owner-node
        for (String fileName : replicationMap.keySet()) {
            // set transfer-endpoint
            InetAddress targetIP = InetAddress.getByName(replicationMap.get(fileName));
            String fileTransferUrl = "http:/" + targetIP + ":8080/replication/transfer";
            System.out.println(fileTransferUrl + ": " + fileName);

            // create file-byteStream
            File file = new File("data/" + fileName);
            byte[] fileBytes = fileService.fileToBytes(file);

            // create Filetransfer-object and serialize
            fileTransfer = new FileTransfer(fileName, fileBytes);
            String fileTransferJson = gson.toJson(fileTransfer);

            // send request
            String response = messageService.postRequest(fileTransferUrl, fileTransferJson, "transfer file");
            System.out.println(response);
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

    public void replication() {

    }
}
