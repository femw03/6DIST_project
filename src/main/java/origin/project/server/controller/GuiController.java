package origin.project.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import origin.project.server.service.MessageService;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.repository.NamingRepository;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@RestController
@RequestMapping("/gui")
public class GuiController {
    @Autowired
    private NamingRepository namingRepository;
    @Autowired
    private MessageService messageService;
    @Autowired
    private NamingServerController namingServerController;

    @GetMapping("/")
    public String dashboard(Model model) {
        // Add necessary data to the model
        model.addAttribute("dashboardContent", "Dashboard content goes here...");
        // Add more data as needed
        model.addAttribute("namingServerIp", "173.18.0.3"); // test, need function to get namingserver IP???
        model.addAttribute("existingNodes", 5);
        // Return the view name (HTML template name)
        return "GUI";
    }

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
            String nodeUrl = "http:/" + ipAddress + ":" + namingServerController.getNodePort() + "/node/get-name";
            String nodeName = messageService.getRequest(nodeUrl, "get node name");
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
            return Integer.parseInt(response);
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
            return Integer.parseInt(response);
        } else {
            // Handle the case where the NamingEntry is not found
            return -1;
        }
    }
}
