package origin.project.client.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.model.dto.LogEntry;
import origin.project.client.repository.LogRepository;

import java.io.File;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Getter
@Service
public class ReplicationService {
    @Autowired
    private Node node;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FileService fileService;

    @Autowired
    private LogRepository logRepository;
    private File localFileFolder;

    //private ArrayList<String> currentLocalFiles;
    private ArrayList<String> currentLocalFiles = new ArrayList<>();

    Logger logger = Logger.getLogger(ReplicationService.class.getName());

    private String replicationBaseUrl;

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
        // allows for local testing
//        System.out.println(node.getNamingServerIp());
//        if (node.getNamingServerIp() == null) {
//            node.setNamingServerIp(InetAddress.getByName("127.0.0.1"));
//        }

        localFileFolder = new File(node.getLOCAL_FILES_PATH());
        // set folder path for data-files (e.g., /data/)
        //currentLocalFiles = new ArrayList<>();

        replicationBaseUrl = "http:/"+node.getNamingServerIp()+":"+node.getNamingServerPort()+"/replication";

        System.out.println("local path: " + localFileFolder);
        System.out.println("replicated path: " + node.getREPLICATED_FILES_PATH());
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

        currentLocalFiles = fileService.scanFolder(localFileFolder, localFileFolder.toPath());

        logger.info("Found following files: " + currentLocalFiles);


        if (currentLocalFiles.isEmpty()) {
            logger.info("Breaking replication start-up because no local files were found");
            return;
        }

        Map<String, String> replicationMap = requestFileLocation(currentLocalFiles);

        // send files to owner-node
        for (String fileName : replicationMap.keySet()) {
            // set transfer-endpoint
            InetAddress targetIP = InetAddress.getByName(replicationMap.get(fileName));

            sendFile(targetIP, fileName, node.getLOCAL_FILES_PATH(), node.getIpAddress());
        }
    }

    public void sendReplicatedFilesToNewNode() throws UnknownHostException {
        // get filename of replicated files
        Map<String,InetAddress> replicatedFiles = new HashMap<>();
        for (LogEntry e : logRepository.findAll()) {
            replicatedFiles.put(e.getFileName(), e.getDownloadLocationID());
        }

        // request replication locations from namingserver
        Map<String, String> replicationMap = requestFileLocation(new ArrayList<>(replicatedFiles.keySet()));


        for (String fileName : replicationMap.keySet()) {
            InetAddress newLocation = InetAddress.getByName(replicationMap.get(fileName));
            if (!newLocation.equals(node.getIpAddress())) {
                // set transfer-endpoint
                InetAddress targetIP = newLocation;
                sendFile(targetIP, fileName, node.getREPLICATED_FILES_PATH(), replicatedFiles.get(fileName));
                logRepository.deleteByFileName(fileName);
                deleteFile(node.getIpAddress(),fileName); // added!!!
            }
        }
    }

    public void updateThread() throws UnknownHostException {
        while(updateThreadRunning) {
            if (node.isNewNode()) {
                logger.info("New node detected in Update-thread.");
                sendReplicatedFilesToNewNode();
                node.setNewNode(false);
            }
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
                        sendFile(targetIP, fileName, node.getLOCAL_FILES_PATH(), node.getIpAddress());
                        currentLocalFiles.add(fileName);
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
                        currentLocalFiles.remove(fileName);
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
        ArrayList<String> newFileList = fileService.scanFolder(localFileFolder, localFileFolder.toPath());

        Map<String, Integer> updatedFilesMap = new HashMap<>();

        // get current files
        System.out.println(newFileList);
        System.out.println(currentLocalFiles);

        // added files = files in newList but not in saved list
        for (String fileName : newFileList) {
            if (!currentLocalFiles.contains(fileName)) {
                updatedFilesMap.put(fileName, 0);
            }
        }

        // deleted files = files in saved list but not in newList
        for (String fileName : currentLocalFiles) {
            if (!newFileList.contains(fileName)) {
                updatedFilesMap.put(fileName, 1);
            }
        }

        return updatedFilesMap;
    }

    public void sendFile(InetAddress targetIP, String fileName, String root, InetAddress downloadLocation) throws UnknownHostException {
        if (targetIP.equals(node.getIpAddress())) {
            return;
        }

        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/transfer-file";
        System.out.println(fileTransferUrl + ": " + fileName);

        // create file-byteStream
        File file = new File(root + "/" + fileName);
        System.out.println(fileTransferUrl + ": " + file.getPath());
        byte[] fileBytes = fileService.fileToBytes(file);

        LogEntry log = new LogEntry(fileName, targetIP, downloadLocation);

        // create Filetransfer-object and serialize
        Gson gson = new Gson();
        FileTransfer fileTransfer = new FileTransfer(fileName, fileBytes, log);
        String fileTransferJson = gson.toJson(fileTransfer);

        // send request
        String response = messageService.postRequest(fileTransferUrl, fileTransferJson, "transfer file");
        System.out.println(response);
    }

    public void deleteFile(InetAddress targetIP, String fileName) throws UnknownHostException {
        // don't send to yourself.
        if (targetIP == node.getIpAddress()) {
            return;
        }

        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/remove-file";
        System.out.println(fileTransferUrl + ": " + fileName);

        // create Filetransfer-object with fileName and serialize
        FileTransfer fileTransfer = new FileTransfer();
        fileTransfer.setFileName(fileName);
        Gson gson = new Gson();
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

    public void processShuttingDown(InetAddress prevNodeIP) throws UnknownHostException {
        // process replicated files : send replicated to previous node
        sendReplicatedFilesToPrevious(prevNodeIP);

        // process local files : if files were download, update location
    }

    public void sendReplicatedFilesToPrevious(InetAddress prevNodeIP) throws UnknownHostException {
        logger.info("Start sending files to previous node");

        File replicatedFileFolder = new File(node.getREPLICATED_FILES_PATH());
        ArrayList<String> replicatedFiles = fileService.scanFolder(replicatedFileFolder, replicatedFileFolder.toPath());

        logger.info("Found replicated files: " + replicatedFiles);

        if (replicatedFiles.isEmpty()) {
            logger.info("Breaking replication shutdown because no replicated files were found");
            return;
        }

        // TCP transfer of all files in fileNames send to IPaddress
        for (String fileName : replicatedFiles) {
            Optional<LogEntry> optionalLogEntry = logRepository.findByFileName(fileName);

            if(optionalLogEntry.isEmpty()) {
                logger.info("ShutDown - Failed to find logEntry for " + fileName);
                return;
            }

            InetAddress downloadLocation = optionalLogEntry.get().getDownloadLocationID();

            // add if statement to prevent sending file to replicated folder of official owner
            sendFile(prevNodeIP, fileName, node.getREPLICATED_FILES_PATH(), downloadLocation);
        }

        /*
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames = fileService.scanFolder(replicatedFileFolder);

        //send to previous
        int preID = node.getPreviousID();
        String URLpre = node.getNamingServerUrl() + "/get-IP-by-hash/" + preID;
        String IPpre = messageService.getRequest(URLpre, "get previous ip");
        IPpre = IPpre.replace("\"", "");              // remove double quotes

        for(String file : fileNames) {
            replicationService.sendFile(InetAddress.getByName(IPpre),file);
        }*/
    }
    public void sendFileToPrevious(FileTransfer fileTransfer) throws UnknownHostException {
        String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + node.getPreviousID();
        String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
        IPprevious = IPprevious.replace("\"", "");              // remove double quotes
        if (IPprevious != null) {
            sendFile(InetAddress.getByName(IPprevious), fileTransfer.getFileName(), node.getLOCAL_FILES_PATH(), fileTransfer.getLogEntry().getDownloadLocationID());
        }
    }

}
