package origin.project.naming.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import origin.project.naming.server.NamingServer;

@RequestMapping("/naming-server")
public class NamingServerController {

    /*@Autowired
    private NamingServer namingServer;

    @PostMapping("/add-node")
    public void addNode(@RequestParam("ipAddress") String ipAddress) {
        namingServer.addNode(ipAddress);
    }

    @PostMapping("/remove-node")
    public void removeNode(@RequestParam("ipAddress") String ipAddress) {
        namingServer.removeNode(ipAddress);
    }

    @GetMapping("/get-node")
    public String getNode(@RequestParam("hashValue") int hashValue) {
        return namingServer.getNode(hashValue);
    }*/
}

