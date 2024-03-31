package origin.project.naming.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import origin.project.naming.repository.NamingEntry;
import origin.project.naming.server.NamingServer;

@Getter
@Setter
@RestController
@RequestMapping("/namingServer")
public class NamingServerController {

    @Autowired
    private NamingServer namingServer;

    @PostMapping("/addNode")
    public ResponseEntity<String> addNode(@RequestParam String nodeName, @RequestParam String ipAddress) {
        try {
            namingServer.addNode(nodeName, ipAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body("Node '" + nodeName + "' added successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/removeNode")
    public ResponseEntity<String> removeNode(@RequestParam String ipAddress) {
        try {
            namingServer.removeNode(ipAddress);
            return ResponseEntity.ok("Node '" + ipAddress + "' removed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint to get node by ip-address
    @GetMapping("/getNode")
    public ResponseEntity<NamingEntry> getNodeByIp(@RequestParam String ipAddress) {
        try {
            NamingEntry node = namingServer.getNodeByIp(ipAddress);
            return ResponseEntity.ok(node);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/getAllNodes")
    public ResponseEntity<Iterable<NamingEntry>> getAllNodes() {
        try {
            return ResponseEntity.ok(namingServer.getAllNodes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/getFileLocation")
    public ResponseEntity<String> getFileLocation(@RequestParam String fileName) {
        try {
            String location = namingServer.getFileLocation(fileName);
            return ResponseEntity.ok("File '" + fileName + "' is located at node with IP: " + location);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/clearRepository")
    public ResponseEntity<String> clearRepository() {
        try {
            namingServer.deleteAll();
            return ResponseEntity.ok("NodeRepository cleared successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

