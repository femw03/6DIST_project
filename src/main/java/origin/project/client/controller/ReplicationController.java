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
import origin.project.client.service.FileService;

import java.nio.file.Path;
import java.util.logging.Logger;

@Getter
@Setter
@RestController
@RequestMapping("/replication")
public class ReplicationController {
    @Autowired
    FileService fileService;

    @Autowired
    Node node;

    Logger logger = Logger.getLogger(origin.project.server.controller.ReplicationController.class.getName());

    @PostMapping("/transfer-file")
    public ResponseEntity<String> nodeSendsFileTransfer(@RequestBody FileTransfer fileTransfer) {
        logger.info("POST: /replication/transfer " + fileTransfer.getFileName());
        String name = fileTransfer.getFileName();

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file-name");
        }
        byte[] file = fileTransfer.getFile();
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file");
        }

        System.out.println(fileTransfer.getLogEntry());
        fileService.createFileFromTransfer(fileTransfer, Path.of(node.getREPLICATED_FILES_PATH()));

        return ResponseEntity.ok("File " + fileTransfer.getFileName() + " received successfully.");
    }

    @DeleteMapping("/remove-file")
    public ResponseEntity<String> removeNode(@RequestBody FileTransfer fileTransfer) {
        String name = fileTransfer.getFileName();

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileName for delete is blank");
        }

        if (!fileService.fileExists(name)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File " + name + " not found.");
        }

        if (fileService.fileDeleted(name)) {
            return new ResponseEntity<>(name + "deleted successfully", HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File " + name + " could not be deleted.");
    }

}
