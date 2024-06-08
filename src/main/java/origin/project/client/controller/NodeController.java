package origin.project.client.controller;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import origin.project.client.Node;
import origin.project.client.model.dto.FileTransfer;
import origin.project.client.model.dto.LogEntry;
import origin.project.client.service.FileService;
import origin.project.server.model.naming.NamingEntry;

import java.io.File;
import java.util.List;

@Getter
@Setter
@RestController
@RequestMapping("/node")
public class NodeController {
    @Autowired
    private Node node;
    @Autowired
    private FileService fileService;
    @GetMapping("/get-name")
    public String getNodeName() {
        return node.getNodeName();
    }

    @GetMapping("/get-localfiles")
    public Iterable<String> getLocalFiles() {
        File localFileFolder = new File(node.getLOCAL_FILES_PATH());
        List<String> localFiles = fileService.scanFolder(localFileFolder, localFileFolder.toPath());
        return localFiles;
    }

    @GetMapping("/get-replicatedfiles")
    public Iterable<String> getReplicatedFiles() {
        File replicatedFileFolder = new File(node.getREPLICATED_FILES_PATH());
        List<String> replicatedFiles = fileService.scanFolder(replicatedFileFolder, replicatedFileFolder.toPath());
        return replicatedFiles;
    }

    @GetMapping("/get-nextID")
    public int getNextID() {
        return node.getNextID();
    }

    @GetMapping("/get-previousID")
    public int getPreviousID() {
        return node.getPreviousID();
    }

}
