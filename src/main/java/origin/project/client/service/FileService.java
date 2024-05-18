package origin.project.client.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.repository.LogRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;


@Getter
@Setter
@Service
public class FileService {
    @Autowired
    private Node node;

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

    public boolean fileExists(String filePath) {
        // Create a Path object
        Path path = Paths.get(filePath);

        // Check if the file exists
        return Files.exists(path);
    }

    public boolean fileDeleted(String filePath) {
        File file = new File(filePath);

        return file.delete();
    }

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
