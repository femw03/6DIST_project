package origin.project.client.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.model.dto.LogEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class FileService {
    @Autowired
    private Node node;
    @Autowired
    private MessageService messageService;
    private Map<String, LogEntry> log = new HashMap<>();


    Logger logger = Logger.getLogger(FileService.class.getName());
    public void createFileFromTransfer(FileTransfer fileTransfer) {
        String fileName = node.getReplicatedFolderPath() + "/" + fileTransfer.getFileName();
        byte[] fileContent = fileTransfer.getFile();

        // If file directory doesn't exist
        String directoryPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.info("Failed to create directory: " + directoryPath);
                return;
            }
        }

        // Create the file
        File file = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileContent);
            System.out.println("File created successfully: " + fileName);
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }

        // Update log with references for the file
        Map<String, LogEntry> log = node.getLog();
        if (log == null) {
            log = new HashMap<>();
        }
        log.put(file.getName(),fileTransfer.getLog());
        node.setLog(log);
    }

    public byte[] fileToBytes(File file) {
        FileInputStream fis = null;
        byte[] bytesArray = new byte[(int) file.length()];

        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray); // Reads file content into bytesArray
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytesArray;
    }

    public void sendFiles(InetAddress targetIP, String fileName) {
        // set transfer-endpoint
        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/transfer";
        System.out.println(fileTransferUrl + ": " + fileName);

        // create file-byteStream
        File file = new File( node.getReplicatedFolderPath() + "/" + fileName);
        byte[] fileBytes = fileToBytes(file);

        // create Filetransfer-object and serialize
        Gson gson = new Gson();
        String URLhash = node.getNamingServerUrl() + "/get-hash-by-IP/" + targetIP;
        String hashIDString = messageService.getRequest(URLhash, "get hashID");
        int targetID = Integer.parseInt(hashIDString);
        LogEntry entry = new LogEntry(file,targetID,node.getCurrentID());
        FileTransfer fileTransfer = new FileTransfer(fileName, fileBytes, entry);
        String fileTransferJson = gson.toJson(fileTransfer);

        // send request
        String response = messageService.postRequest(fileTransferUrl, fileTransferJson, "transfer file");
        System.out.println(response);
    }
}
