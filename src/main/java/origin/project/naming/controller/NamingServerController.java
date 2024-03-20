package origin.project.naming.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import origin.project.naming.repository.NamingRepository;
import java.util.Optional;

@Getter
@Setter
@RestController
@RequestMapping("/naming-server")
public class NamingServerController {

    @Autowired
    private NamingRepository namingRepository;

//    @Autowired
//    public NamingServerController(NamingRepository namingRepository) {
//        this.namingRepository = namingRepository;
//    }

    @PostMapping("/add-node")
    public void addNode(@RequestParam("ipAddress") String ipAddress) {
//        NamingEntry namingEntry = new NamingEntry();
//        namingRepository.save(namingEntry);
    }

    @PostMapping("/remove-node")
    public void removeNode(@RequestParam("ipAddress") String ipAddress) {
//        namingRepository.delete(ipAddress);
    }

    @GetMapping("/get-node")
    public Optional<String> getNode(@RequestParam("hashValue") int hashValue) {
//        Optional<String> ip = namingRepository.findById(hashValue);
//        if(ip.isEmpty()){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Hash not found!");
//        }
//        return ip;
        return null;
    }
}

