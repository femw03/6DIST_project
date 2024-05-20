package origin.project.client.service;

import lombok.Getter;
import lombok.Setter;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


@Getter
@Setter
@Service
public class FileService {
    @Autowired
    private Node node;
    @Autowired
    private MessageService messageService;
    private Path dataBaseFolder;

    Logger logger = Logger.getLogger(FileService.class.getName());
    public void createFileFromTransfer(FileTransfer fileTransfer) {
        String fileName =  node.getReplicatedFolderPath() + "/" + fileTransfer.getFileName();
        System.out.println(fileName);
        byte[] fileContent = fileTransfer.getFile();

        // if file directory doesn't exist
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
        node.getReplicatedLog().put(fileName,fileTransfer.getDownloadLocation());
        logger.info("Repliacted files log: "+node.getReplicatedLog());
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

    public void sendFiles(InetAddress targetIP, String fileName, String pathFolder) throws UnknownHostException {
        // don't send to yourself.
        if (targetIP.equals(node.getIpAddress())) {
            return;
        }

        // set transfer-endpoint
        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/transfer-file";
        System.out.println(fileTransferUrl + ": " + fileName);

        // create file-byteStream
        File file = new File(pathFolder + "/" + fileName);
        byte[] fileBytes = fileToBytes(file);

        // create Filetransfer-object and serialize
        Gson gson = new Gson();

        FileTransfer fileTransfer = new FileTransfer(fileName, fileBytes, node.getIpAddress());
        String fileTransferJson = gson.toJson(fileTransfer);

        // send request
        String response = messageService.postRequest(fileTransferUrl, fileTransferJson, "transfer file");
        System.out.println(response);
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
                    Path relativePath = dataBaseFolder.relativize(target);

                    fileNames.add(relativePath.toString());
                }
            }
        }
    }

    public boolean fileExists(String fileName) {
        // Create a Path object
        Path path = Paths.get(dataBaseFolder + fileName);

        // Check if the file exists
        return Files.exists(path);
    }

    public boolean fileDeleted(String fileName) {
        File file = new File(dataBaseFolder + fileName);
        return file.delete();
    }

    public void relocateFile(String fileName) {
        // remove file from replicated folder + put it in local folder
    }
}
