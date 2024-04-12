package origin.project.naming.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import origin.project.naming.model.naming.NamingEntry;
import origin.project.naming.model.naming.dto.NodeRequest;
import origin.project.naming.repository.NamingRepository;
import origin.project.naming.service.JsonService;
import origin.project.naming.service.NamingService;

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

    private static final String FILE_PATH = "resources/nodes.json";

    @Autowired
    private NamingRepository namingRepository;

    Logger logger = Logger.getLogger(NamingServerController.class.getName());

    @Autowired
    private NamingService namingService;
    @Autowired
    private JsonService jsonService;

    @GetMapping("/all-nodes")
    public Iterable<NamingEntry> getNamingEntries() {
        logger.info("GET: /users");
        List<NamingEntry> entries = (List<NamingEntry>) namingRepository.findAll();
        return entries
                .stream()
                .toList();
    }
    @PostMapping("/add-node")
    public int addNode(@RequestBody NodeRequest nodeReq) {
        logger.info("POST: /add-node/"+ nodeReq.toString());
        String name = nodeReq.getName();
        String ipAddress = nodeReq.getIp();

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
        System.out.println(namingEntry.getIP());
        jsonService.addEntryToJsonFile(FILE_PATH, namingEntry);
        namingRepository.save(namingEntry);
        return hash;
    }

    @DeleteMapping("/remove-node")
    public void removeNode(@RequestBody NodeRequest nodeRequest) {
        logger.info("DELETE: /node/" + nodeRequest.toString());
        String ipAddress = nodeRequest.getIp();
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
    }

    @GetMapping("/get-node/{hashValue}")
    public Optional<NamingEntry> getNode(@PathVariable("hashValue") int hashValue) {
        logger.info("GET: /get-node/"+ hashValue);
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashValue);
        if(optionalEntry.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Hash not found!");
        }
        return optionalEntry;
    }

    @GetMapping("/file-location/{file-name}")
    public String getFileLocation(@PathVariable("file-name") String fileName) {
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

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return errors;
//    }
}

