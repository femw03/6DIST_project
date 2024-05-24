package origin.project.client.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.repository.LogRepository;
import origin.project.client.service.FileService;
import origin.project.client.service.ReplicationService;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Getter
@Setter
@RestController
@RequestMapping("/replication")
public class ReplicationController {
    @Autowired
    private FileService fileService;
    @Autowired
    private Node node;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private ReplicationService replicationService;

    Logger logger = Logger.getLogger(origin.project.server.controller.ReplicationController.class.getName());

    @PostMapping("/transfer-file")
    public ResponseEntity<String> nodeReceivesFileTransfer(@RequestBody FileTransfer fileTransfer) throws UnknownHostException {
        logger.info("POST: /replication/transfer " + fileTransfer.getFileName());
        String name = fileTransfer.getFileName();

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file-name");
        }
        byte[] file = fileTransfer.getFile();
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file");
        }

        if (replicationService.getCurrentLocalFiles().contains(name)){
            logger.info("Local file ("+ name +") received from node in shutdown, sending to previous node");
            replicationService.sendFileToPrevious(fileTransfer);
            return ResponseEntity.ok("File " + fileTransfer.getFileName() + " send to previous successfully.");
        }

        fileService.createFileFromTransfer(fileTransfer, Path.of(node.getREPLICATED_FILES_PATH()));

        // Add to log
        logRepository.save(fileTransfer.getLogEntry());
        logger.info("Current log-entries: " + logRepository.findAll());

        return ResponseEntity.ok("File " + fileTransfer.getFileName() + " received successfully.");
    }

    @DeleteMapping("/remove-file")
    public ResponseEntity<String> nodeInformsToRemoveFile(@RequestBody FileTransfer fileTransfer) {
        logger.info("remove-file " + fileTransfer);
        String name = fileTransfer.getFileName();

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileName for delete is blank");
        }

        String filePath = node.getREPLICATED_FILES_PATH() + "/" + name;
        logger.info("Attempt remove-file for " + filePath);

        if (!fileService.fileExists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File " + name + " not found in path " + filePath + ".");
        }
        logger.info(filePath + " exists");

        if (!fileService.fileDeleted(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File " + filePath + " could not be deleted.");
        }
        logger.info(filePath + " deleted");

        if (!logRepository.existsByFileName(name)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to delete log-entry for " + filePath + ".");
        }
        logger.info("log-entry for " + filePath + " deleted");

        logRepository.deleteByFileName(name);
        return new ResponseEntity<>(name + " deleted successfully.", HttpStatus.OK);
    }

}
