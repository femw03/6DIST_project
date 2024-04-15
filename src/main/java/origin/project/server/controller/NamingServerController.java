package origin.project.server.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.model.naming.dto.NodeRequest;
import origin.project.server.repository.NamingRepository;
import origin.project.server.service.JsonService;
import origin.project.server.service.NamingService;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;


@Getter
@Setter
@RestController
@RequestMapping("/naming-server")
public class NamingServerController {
    @Autowired
    private NamingRepository namingRepository;
    @Autowired
    private NamingService namingService;
    @Autowired
    private JsonService jsonService;
    Logger logger = Logger.getLogger(NamingServerController.class.getName());
    private static final String FILE_PATH = "src/main/resources/nodes.json";


    @GetMapping("/all-nodes")
    public Iterable<NamingEntry> getNamingEntries() {
        logger.info("GET: /users");
        List<NamingEntry> entries = (List<NamingEntry>) namingRepository.findAll();
        return entries
                .stream()
                .toList();
    }
    @PostMapping("/add-node")
    public ResponseEntity<String> addNode(@RequestBody NodeRequest nodeReq) {
        logger.info("POST: /add-node/"+ nodeReq.toString());
        String name = nodeReq.getName();
        InetAddress ipAddress = nodeReq.getIp();

        if (ipAddress == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ERROR: ip cannot be null.");
        }
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: name cannot be null.");
        }

        int hash = namingService.hashingFunction(name);

        if (namingRepository.existsById(hash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: Node with name '" + name + "' already exists.");
        }
        if (namingRepository.existsByIP(ipAddress)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: Node with ip-address '" + ipAddress + "' already exists.");
        }

        NamingEntry namingEntry = new NamingEntry(hash, ipAddress);
        //System.out.println(namingEntry.getIP());
        jsonService.addEntryToJsonFile(FILE_PATH, namingEntry);
        namingRepository.save(namingEntry);
        return ResponseEntity.ok("Node with hashID "+ hash + " successfully created!");
    }

    @DeleteMapping("/remove-node")
    public ResponseEntity<String> removeNode(@RequestBody NodeRequest nodeRequest) {
        logger.info("DELETE: /node/" + nodeRequest.toString());
        InetAddress ipAddress = nodeRequest.getIp();
        String name = nodeRequest.getName();
        int hash = nodeRequest.getHash();

        if (ipAddress == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ERROR: ip cannot be null.");
        }
        if (name == null && hash == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: name or hash is required.");
        }

        if (hash == -1) {
            hash = namingService.hashingFunction(name);
        }

        Optional<NamingEntry> optionalEntry = namingRepository.findById(hash);
        if(optionalEntry.isPresent()) {
            if (Objects.equals(optionalEntry.get().getIP(), ipAddress)) {
                namingRepository.deleteById(hash);
                jsonService.removeEntryFromJsonFile(FILE_PATH, new NamingEntry(hash, ipAddress));
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR : Combination of name and IP does not exist.");
            }
        }
        else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR : No node with given name.");
        }
        return ResponseEntity.ok("Node with hashID "+ hash + " successfully removed!");
    }

    @GetMapping("/get-node-by-hash/{hashValue}")
    public Optional<NamingEntry> getNode(@PathVariable("hashValue") int hashValue) {
        logger.info("GET: /get-node/"+ hashValue);
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashValue);
        if(optionalEntry.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Hash not found!");
        }
        return optionalEntry;
    }

    @GetMapping("/get-node-by-name/{name}")
    public Optional<NamingEntry> getNode(@PathVariable("name") String name) {
        logger.info("GET: /get-node/"+ name);
        int hashValue = namingService.hashingFunction(name);
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashValue);
        if(optionalEntry.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Name not found!");
        }
        return optionalEntry;
    }

    @GetMapping("/file-location/{file-name}")
    public InetAddress getFileLocation(@PathVariable("file-name") String fileName) {
        logger.info("GET /file-location/" + fileName);
        if (namingRepository.count() < 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No nodes available in the system.");
        }
        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR: name cannot be null.");
        }
        int fileHash = namingService.hashingFunction(fileName);
        ArrayList<NamingEntry> nodesWithHashSmallerThanFile = (ArrayList<NamingEntry>) namingRepository.findByHashLessThan(fileHash);

        if (nodesWithHashSmallerThanFile.isEmpty()) {
            // If no nodes with hash smaller than file hash, find the node with the biggest hash
            Optional<NamingEntry> ownerNodeOptional = namingRepository.findEntryWithLargestHash();
            if (ownerNodeOptional.isPresent()) {
                NamingEntry ownerNode = ownerNodeOptional.get();
                logger.info(fileName + " gave file-hash " + fileHash + " and returned " + ownerNode);
                return ownerNode.getIP();
            }
            else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ".");
            }
        }
        else {
            // Find the node with the smallest difference between its hash and the file hash
            NamingEntry ownerNode = namingService.findNearestNodeId(fileHash,nodesWithHashSmallerThanFile);
            logger.info(fileName + " gave file-hash " + fileHash + " and returned " + ownerNode);
            return ownerNode.getIP();
        }

    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearRepository() {
        try {
            namingRepository.deleteAll();
            jsonService.clearJsonFile(FILE_PATH);
            return ResponseEntity.ok("Cleared successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-hash/{name}")
    public int getHashID(@PathVariable("name") String name) {
        return namingService.hashingFunction(name);
    }


}