package origin.project.client.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.model.dto.LogEntry;
import origin.project.server.controller.NamingServerController;

import java.io.File;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class ReplicationService {
    @Autowired
    private Node node;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FileService fileService;

    private File localFileFolder;

    private ArrayList<String> fileNames;

    Logger logger = Logger.getLogger(NamingServerController.class.getName());

    private String replicationBaseUrl;

    boolean updateThreadRunning = false;

    @Value("${localfiles.path}")
    private String FOLDER_PATH;



    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (!node.isDiscoveryFinished() && node.getExistingNodes() < 2) {
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
        // set folder path for data-files (e.g., /data/)
        fileService.setDataBaseFolder(Paths.get(FOLDER_PATH));
        fileNames = new ArrayList<>();

        replicationBaseUrl = "http:/"+node.getNamingServerIp()+":"+node.getNamingServerPort()+"/replication";

        // verify local files and send hash-values to naming server
        startUp();

        // start update thread
        updateThreadRunning = true;
        updateThread();



    }


    // Starting
    //  - Verify local files
    //  - Report hash-values to naming server
    //  - Transfer files to the owner-nodes.
    public void startUp() throws UnknownHostException {
        // get current files.

        fileService.scanFolder(localFileFolder, fileNames);

        logger.info("Found following files: " + fileNames);


        if (fileNames.isEmpty()) {
            logger.info("Breaking replication start-up because no local files were found");
            return;
        }

        Map<String, String> replicationMap = requestFileLocation(fileNames);

        // send files to owner-node
        for (String fileName : replicationMap.keySet()) {
            // set transfer-endpoint
            InetAddress targetIP = InetAddress.getByName(replicationMap.get(fileName));

            sendFile(targetIP, fileName);
        }
    }

    public void updateThread() {
        while(updateThreadRunning) {
            // check updates
            Map<String, Integer> updatedFiles = findUpdates();
            logger.info("found update :" + updatedFiles.keySet());

            // report updates
            for (String fileName : updatedFiles.keySet()) {
                if (updatedFiles.get(fileName) == 0) {
                    try {
                        ArrayList<String> names = new ArrayList<>();
                        names.add(fileName);
                        Map<String, String> replicationMap = requestFileLocation(names);
                        InetAddress targetIP = InetAddress.getByName(replicationMap.get(fileName));

                        // send file
                        sendFile(targetIP, fileName);
                        fileNames.add(fileName);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                }
                    // delete file
                if (updatedFiles.get(fileName) == 1) {
                    try {
                        ArrayList<String> names = new ArrayList<>();
                        names.add(fileName);
                        Map<String, String> replicationMap = requestFileLocation(names);
                        InetAddress targetIP = InetAddress.getByName(replicationMap.get(fileName));

                        // send file
                        deleteFile(targetIP, fileName);
                        fileNames.remove(fileName);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

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
        fileService.scanFolder(localFileFolder, newFileList);
        System.out.println(newFileList);
        System.out.println(fileNames);

        // added files = files in newList but not in saved list
        for (String fileName : newFileList) {
            if (!fileNames.contains(fileName)) {
                updatedFilesMap.put(fileName, 0);
            }
        }

        // deleted files = files in saved list but not in newList
        for (String fileName : fileNames) {
            if (!newFileList.contains(fileName)) {
                updatedFilesMap.put(fileName, 1);
            }
        }

        return updatedFilesMap;
    }

    public void sendFile(InetAddress targetIP, String fileName) throws UnknownHostException {
        Gson gson = new Gson();

        // don't send to yourself.
        if (targetIP.equals(node.getIpAddress())) {
            return;
        }

        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/transfer-file";
        System.out.println(fileTransferUrl + ": " + fileName);

        // create file-byteStream
        File file = new File("data/" + fileName);
        byte[] fileBytes = fileService.fileToBytes(file);

        LogEntry log = new LogEntry(fileName, targetIP, node.getIpAddress());
        // create Filetransfer-object and serialize
        FileTransfer fileTransfer = new FileTransfer(fileName, fileBytes, log);
        String fileTransferJson = gson.toJson(fileTransfer);

        // send request
        String response = messageService.postRequest(fileTransferUrl, fileTransferJson, "transfer file");
        System.out.println(response);
    }

    public void deleteFile(InetAddress targetIP, String fileName) throws UnknownHostException {
        Gson gson = new Gson();

        // don't send to yourself.
        if (targetIP == node.getIpAddress()) {
            return;
        }

        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/remove-file";
        System.out.println(fileTransferUrl + ": " + fileName);

        // create Filetransfer-object with fileName and serialize
        FileTransfer fileTransfer = new FileTransfer();
        fileTransfer.setFileName(fileName);
        String fileTransferJson = gson.toJson(fileTransfer);

        // send request
        String response = messageService.deleteRequest(fileTransferUrl, fileTransferJson, "delete file");
        System.out.println(response);
    }

    public Map<String, String> requestFileLocation(ArrayList<String> fileNames) {
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
        return new Gson().fromJson(replicationMapJSON, type);
    }
}
