package origin.project.client.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.model.dto.LogEntry;
import origin.project.client.repository.LogRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


@Getter
@Setter
@Service
public class FileService {
    @Autowired
    private Node node;
    //private Map<String, LogEntry> log = new HashMap<>(); // try to use h2 database instead!!! Makes searching easier!!!!
    @Autowired
    private LogRepository logRepository;
    private Path dataBaseFolder;

    Logger logger = Logger.getLogger(FileService.class.getName());

    public void createFileFromTransfer(FileTransfer fileTransfer, Path folder) {
        String fileName =  folder + "/" + fileTransfer.getFileName();
        System.out.println(fileName);
        byte[] fileContent = fileTransfer.getFile();

        // if file directory doesn't exist
        String directoryPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
        File directory = new File(directoryPath);
        System.out.println(directory);
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

        // Add to log
        logRepository.save(fileTransfer.getLogEntry());
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

    /**
     * Function to recursively scan a folder for files.
     *
     * @param folder    folder to scan
     * @param root      path to which to relativize (e.g., /local-files or /replicated-files)
     * @return
     */
    public ArrayList<String> scanFolder(File folder, Path root) {
        ArrayList<String> fileNames = new ArrayList<>();
        // files in the folder
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileNames.addAll(scanFolder(file, root));
                } else {

                    Path target = Paths.get(file.getPath());
                    Path relativePath = root.relativize(target);

                    fileNames.add(relativePath.toString());
                }
            }
        }
        return fileNames;
    }

    public boolean fileExists(String fileName) {
        // Create a Path object
        Path path = Paths.get(node.getREPLICATED_FILES_PATH() + "/" + fileName);

        // Check if the file exists
        return Files.exists(path);
    }

    public boolean fileDeleted(String fileName) {
        File file = new File(node.getREPLICATED_FILES_PATH() + "/" + fileName);

        return file.delete();
    }

    /*public void sendFiles(InetAddress targetIP, String fileName) throws UnknownHostException {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        if (targetIP.equals(node.getIpAddress())){
            return;
        }

        // set transfer-endpoint
        String fileTransferUrl = "http:/" + targetIP + ":8080/replication/transfer-file";
        System.out.println(fileTransferUrl + ": " + fileName);

        // create file-byteStream
        File file = new File( node.getFolderPath() + "/" + fileName);
        byte[] fileBytes = fileToBytes(file);

        // create Filetransfer-object and serialize
        String URLhash = node.getNamingServerUrl() + "/get-hash-by-IP/" + targetIP.getHostAddress();
        String hashIDString = messageService.getRequest(URLhash, "get hashID");
        int targetID = Integer.parseInt(hashIDString);
        LogEntry entry = new LogEntry(fileName, node.getIpAddress(), targetIP);
        logger.info("filetransfer: " + fileName + " " + entry + " " + gson.toJson(entry));// + " " + fileBytes + " " + gson.toJson(entry));
        FileTransfer fileTransfer = new FileTransfer(fileName, fileBytes, entry);
        logger.info("filetransfer : " + fileTransfer);
        String fileTransferJson = gson.toJson(fileTransfer);

        // send request
        String response = messageService.postRequest(fileTransferUrl, fileTransferJson, "transfer file");
        System.out.println(response);
    }*/

    public boolean moveFile(String fileName, String destination) throws IOException {
        // if file directory doesn't exist
        String directoryPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
        File directory = new File(directoryPath);
        System.out.println("Entered move file! directory : " + directory);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.info("Failed to create directory: " + directoryPath);
                return false;
            }
        }
        // Path to the source file
        Path sourceFile = Paths.get(fileName);

        // Path to the target directory
        Path targetDirectory = Paths.get(destination);

        Path movedFile = Files.move(sourceFile, targetDirectory.resolve(sourceFile.getFileName()));
        return movedFile != null;
    }
}
