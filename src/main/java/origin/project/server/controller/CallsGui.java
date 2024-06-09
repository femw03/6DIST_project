package origin.project.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import origin.project.server.model.naming.NamingEntry;
import origin.project.server.repository.NamingRepository;
import origin.project.server.service.MessageService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
    }

    @PostMapping("/activate-node/{name}/{ip}")
    public ResponseEntity<String> activateNode(@PathVariable String name, @PathVariable String ip) {
        String remoteHost = ip;  // The IP of the remote node
        String user = "2053 root";  // SSH username for the remote node
        //String password = "your-password";  // SSH password for the remote node

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, remoteHost, 8080);
            //session.setPassword(password);

            // Avoid asking for key confirmation
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            // Construct the command
            String command = String.format("java -jar ProjectYNode.jar \"%s\" \"%s\"", name, ip);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            channel.setInputStream(null);
            InputStream in = channel.getInputStream();

            channel.connect();

            // Read the output from the command
            StringBuilder output = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    int exitStatus = channel.getExitStatus();
                    if (exitStatus == 0) {
                        return ResponseEntity.ok("Node activated successfully:\n" + output.toString());
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error occurred while activating the node:\n" + output.toString());
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Exception occurred: " + e.getMessage());
        }
    }*/
}
