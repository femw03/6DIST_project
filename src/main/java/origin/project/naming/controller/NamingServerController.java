package origin.project.naming.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import origin.project.naming.repository.NamingRepository;
import origin.project.naming.server.NamingServer;

import javax.swing.text.html.Option;
import java.util.Optional;

@RestController
@RequestMapping("/naming-server")
public class NamingServerController {

    @Autowired
    private NamingRepository namingRepository;

    @PostMapping("/add-node")
    public void addNode(@RequestParam("ipAddress") String ipAddress) {
        namingRepository.save(ipAddress);
    }

    @PostMapping("/remove-node")
    public void removeNode(@RequestParam("ipAddress") String ipAddress) {
        namingRepository.delete(ipAddress);
    }

    @GetMapping("/get-node")
    public Optional<String> getNode(@RequestParam("hashValue") int hashValue) {
        Optional<String> ip = namingRepository.findById(hashValue);
        if(ip.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Hash not found!");
        }
        return ip;
    }
}

