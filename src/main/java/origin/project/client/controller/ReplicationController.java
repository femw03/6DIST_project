package origin.project.client.controller;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.service.FileService;
import java.util.logging.Logger;

@Getter
@Setter
@RestController
@RequestMapping("/replication")
public class ReplicationController {
    @Autowired
    FileService fileService;

    Logger logger = Logger.getLogger(origin.project.server.controller.ReplicationController.class.getName());

    @PostMapping("/transfer")
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

        fileService.createFileFromTransfer(fileTransfer);

        return ResponseEntity.ok("File " + fileTransfer.getFileName() + " received successfully.");
    }

}
