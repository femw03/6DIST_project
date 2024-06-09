package origin.project.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.repository.NamingRepository;
import origin.project.server.service.MessageService;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Getter
@Setter
@RestController
@RequestMapping("/gui")
public class CallsGui {
    @Autowired
    private NamingRepository namingRepository;
    @Autowired
    private MessageService messageService;
    @Autowired
    private NamingServerController namingServerController;
    Logger logger = Logger.getLogger(NamingServerController.class.getName());

    @GetMapping("/get-nodeLocalFiles/{hashID}")
    public Iterable<String> getNodeLocalFiles(@PathVariable("hashID") int hashID) {
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashID);
        if (optionalEntry.isPresent()) {
            InetAddress ipAddress = optionalEntry.get().getIP();
            String nodeUrl = "http:/" + ipAddress + ":" + namingServerController.getNodePort() + "/node/get-localfiles";
            String response = messageService.getRequest(nodeUrl, "get local files");
            // Parse the JSON response to a list of strings
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(response, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                e.printStackTrace();
                // Handle parsing exceptions
                return List.of();
            }
        } else {
            // Handle the case where the NamingEntry is not found
            return List.of();
        }
    }

    @GetMapping("/get-nodeReplicatedFiles/{hashID}")
    public Iterable<String> getNodeReplicatedFiles(@PathVariable("hashID") int hashID) {
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashID);
        if (optionalEntry.isPresent()) {
            InetAddress ipAddress = optionalEntry.get().getIP();
            String nodeUrl = "http:/" + ipAddress + ":" + namingServerController.getNodePort() + "/node/get-replicatedfiles";
            String response = messageService.getRequest(nodeUrl, "get replicated files");
            // Parse the JSON response to a list of strings
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(response, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                e.printStackTrace();
                // Handle parsing exceptions
                return List.of();
            }
        } else {
            // Handle the case where the NamingEntry is not found
            return List.of();
        }
    }

    @GetMapping("/get-nodeName/{hashID}")
    public String getNodeName(@PathVariable("hashID") int hashID) {
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashID);
        if (optionalEntry.isPresent()) {
            InetAddress ipAddress = optionalEntry.get().getIP();
            logger.info("get nodeName ipaddress called: " + ipAddress);
            String nodeUrl = "http:/" + ipAddress + ":" + namingServerController.getNodePort() + "/node/get-name";
            String nodeName = messageService.getRequest(nodeUrl, "get node name");
            logger.info("nodename returned by request: " + nodeUrl + "= " + nodeName);
            return nodeName;
        } else {
            // Handle the case where the NamingEntry is not found
            return "Node not found";
        }
    }

    @GetMapping("/get-nodeNextID/{hashID}")
    public int getNodeNextID(@PathVariable("hashID") int hashID) {
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashID);
        if (optionalEntry.isPresent()) {
            InetAddress ipAddress = optionalEntry.get().getIP();
            String nodeUrl = "http:/" + ipAddress + ":" + namingServerController.getNodePort() + "/node/get-nextID";
            String response = messageService.getRequest(nodeUrl, "get node next ID");
            //if(response != null) {
                return Integer.parseInt(response);
            //}
            //return hashID;
        } else {
            // Handle the case where the NamingEntry is not found
            return -1;
        }
    }

    @GetMapping("/get-nodePreviousID/{hashID}")
    public int getNodePreviousID(@PathVariable("hashID") int hashID) {
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashID);
        if (optionalEntry.isPresent()) {
            InetAddress ipAddress = optionalEntry.get().getIP();
            String nodeUrl = "http:/" + ipAddress + ":" + namingServerController.getNodePort() + "/node/get-previousID";
            String response = messageService.getRequest(nodeUrl, "get node previous ID");
            //if(response != null) {
                return Integer.parseInt(response);
            //}
            //return hashID;
        } else {
            // Handle the case where the NamingEntry is not found
            return -1;
        }
    }

    /*@PostMapping("/kill-node/{hashID}")
    public void killNode(@PathVariable("hashID") int hashID) {
        Optional<NamingEntry> optionalEntry = namingRepository.findById(hashID);
        if(optionalEntry.isPresent()) {
            InetAddress ipAddress = optionalEntry.get().getIP();
            String nodeUrl = "http:/" + ipAddress + ":" + namingServerController.getNodePort() + "/node/kill-node";
            String response = messageService.getRequest(nodeUrl, "kill node");
            logger.info("node " + ipAddress + " killed");
        }
    }*/
}
