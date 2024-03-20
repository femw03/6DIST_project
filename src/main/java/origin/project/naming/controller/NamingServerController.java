package origin.project.naming.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import origin.project.naming.model.AddNodeRequest;
import origin.project.naming.model.NamingEntry;
import origin.project.naming.repository.NamingRepository;
import origin.project.naming.service.Hasher;

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

    @PostMapping("/add-file")
    public void addFile(@RequestParam("fileName") String fileName) {
        int hash = Hasher.hash(fileName);
    }

    @PostMapping("/add-node")
    public void addNode(@RequestBody AddNodeRequest request) {
        int hash = Hasher.hash(request.getName());
        if(!namingRepository.existsById(hash)){
            namingRepository.save(new NamingEntry(hash,request.getIp()));
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Node exists");
        }
    }

    @PostMapping("/remove-node")
    public void removeNode(@RequestParam("ipAddress") String ipAddress) {
//        namingRepository.delete(ipAddress);
    }

    @GetMapping("/get-node")
    public Optional<NamingEntry> getNode(@RequestParam("name") String name) {
        Optional<NamingEntry> ip = namingRepository.findById(Hasher.hash(name));
        if(ip.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Hash not found!");
        }
        return ip;
    }
}

