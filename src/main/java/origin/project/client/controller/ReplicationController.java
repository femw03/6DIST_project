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
import origin.project.client.service.MessageService;
import origin.project.client.service.ReplicationService;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
    private ReplicationService replicationService;
    @Autowired
    private MessageService messageService;

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

        ArrayList<String> fileNames = new ArrayList<>();
        File localFileFolder = new File(node.getFolderPath());
        replicationService.scanFolder(localFileFolder, fileNames);
        logger.info("Found local files: " + fileNames);

        if (fileNames.contains(name)) {         // file already stored locally on node
            String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + node.getPreviousID();
            String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
            // Because of GET request, IP is converted to string with extra double quotes ("")
            IPprevious = IPprevious.replace("\"", "");              // remove double quotes
            InetAddress IPpreviousInet =  InetAddress.getByName(IPprevious);
            fileService.sendFiles(IPpreviousInet,name);
        } else {
            fileService.createFileFromTransfer(fileTransfer);
        }

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
