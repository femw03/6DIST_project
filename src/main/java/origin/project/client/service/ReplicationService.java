package origin.project.client.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jade.wrapper.StaleProxyException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.model.dto.LogEntry;
import origin.project.client.repository.LogRepository;

import java.io.File;
import java.io.IOException;
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
    private ArrayList<String> currentLocalFiles = new ArrayList<>();
    Logger logger = Logger.getLogger(ReplicationService.class.getName());
    private String replicationBaseUrl;
    boolean updateThreadRunning = false;

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
            } catch (UnknownHostException | StaleProxyException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }

    public void actualInit() throws UnknownHostException{
        localFileFolder = new File(node.getLOCAL_FILES_PATH());
        // set folder path for data-files (e.g., /data/)
        //currentLocalFiles = new ArrayList<>();

        replicationBaseUrl = "http:/"+node.getNamingServerIp()+":"+node.getNamingServerPort()+"/replication";

        // verify local files and send hash-values to naming server
        startUp();
        new Thread(() -> {
            try {
                node.startAgents(this);
            } catch (StaleProxyException e) {
                throw new RuntimeException(e);
            }
        }).start(); // pass replicationService to Agent to update fileList.

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

        logger.info("Found following local files: " + currentLocalFiles);

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
                deleteFile(node.getIpAddress(),fileName);
            }
        }
    }

    public void sendLocalFilesToNewNode() throws UnknownHostException {
        // request replication locations from namingserver
        Map<String, String> replicationMap = requestFileLocation(new ArrayList<>(currentLocalFiles));

        for (String fileName : currentLocalFiles) {
            InetAddress newLocation = InetAddress.getByName(replicationMap.get(fileName));
            if (!newLocation.equals(node.getIpAddress())) {
                // set transfer-endpoint
                InetAddress targetIP = newLocation;
                sendFile(targetIP, fileName, node.getLOCAL_FILES_PATH(), node.getIpAddress());
            }
        }
    }

    public void updateThread() throws UnknownHostException {
        while(updateThreadRunning) {
            if (node.isNewNode()) {
                logger.info("New node detected in Update-thread.");
                sendReplicatedFilesToNewNode();
                sendLocalFilesToNewNode();
                node.setNewNode(false);
            }
            // check updates
            Map<String, Integer> updatedFiles = findUpdates();
            logger.info("Found updates:" + updatedFiles.keySet());

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
        logger.info("Updated file list: "+newFileList);
        logger.info("Current file list: "+currentLocalFiles);

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
        Gson gson = new Gson();
        if (targetIP.equals(node.getIpAddress())) {
            return;
        }

        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/transfer-file";
        logger.info(fileTransferUrl + ": " + fileName);

        // create file-byteStream
        File file = new File(root + "/" + fileName);
        byte[] fileBytes = fileService.fileToBytes(file);

        LogEntry log = new LogEntry(fileName, targetIP, downloadLocation);

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
        logger.info(fileTransferUrl + ": " + fileName);

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
        logger.info("FileNames JSON: " + hashedFileNamesJSON);

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

    }
    public void sendFileToPrevious(FileTransfer fileTransfer) throws UnknownHostException {
        String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + node.getPreviousID();
        String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
        IPprevious = IPprevious.replace("\"", "");              // remove double quotes
        if (IPprevious != null) {
            sendFile(InetAddress.getByName(IPprevious), fileTransfer.getFileName(), node.getLOCAL_FILES_PATH(), fileTransfer.getLogEntry().getDownloadLocationID());
        }
    }

    public void resolveFilesDuringFailure(String failingNodeIP) throws IOException {
        // resolve the local files that were replicated in the failing node.
        resolveLocalFilesDuringfailure();
        // resolve the replicated files that were local files of the failing node.
        resolveReplicatedFilesDuringfailure(failingNodeIP);
    }

    private void resolveReplicatedFilesDuringfailure(String failingNodeIP) throws IOException {
        logger.info("Resolving replicated files that were owned by the failing node.");
        // get filenames of replicated files that were local files from the failing node
        for (LogEntry e : logRepository.findAll()) {
            if (e.getDownloadLocationID().toString().equals("/" + failingNodeIP)) {
                String fileName = e.getFileName();

                logger.info("Moving file: " +fileName);
                // move the file from the replicated folder to the local folder
                // the update phase will replicate the file again
                String sourceFile = node.getREPLICATED_FILES_PATH() + "/" + fileName;
                String destinationFile = node.getLOCAL_FILES_PATH() + "/" + fileName;
                fileService.moveFile(sourceFile, destinationFile);

                // Remove the file from the log
                logRepository.deleteByFileName(fileName);
            }
        }
    }

    private void resolveLocalFilesDuringfailure() throws UnknownHostException {
        logger.info("Resolving local files that were replicated in the failing node.");
        // get the current local files
        currentLocalFiles = fileService.scanFolder(localFileFolder, localFileFolder.toPath());

        // check if there are local files
        if (!currentLocalFiles.isEmpty()) {
            return;
        }

        // rereplicate all the local files
        Map<String, String> replicationMap = requestFileLocation(currentLocalFiles);

        // send files to owner-node
        for (String fileName : replicationMap.keySet()) {
            // Set the
            // set transfer-endpoint
            InetAddress targetIP = InetAddress.getByName(replicationMap.get(fileName));
            sendFile(targetIP, fileName, node.getLOCAL_FILES_PATH(), node.getIpAddress());
        }
    }
}
