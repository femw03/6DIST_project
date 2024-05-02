package origin.project.server.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import origin.project.server.service.NamingService;

import java.util.logging.Logger;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RestController
@RequestMapping("/replication")
public class ReplicationController {
    @Autowired
    NamingService namingService;

    Logger logger = Logger.getLogger(ReplicationController.class.getName());

    @PostMapping("/initial-list")
    public ResponseEntity<Map<String, InetAddress>> nodeReportsInitialFiles(@RequestBody String fileNamesJSON) {
        Map<String, InetAddress> replicationMap = new HashMap<>();

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        ArrayList<String> fileNames = gson.fromJson(fileNamesJSON, type);

        for (String fileName : fileNames) {
            int hash = namingService.hashingFunction(fileName);
            InetAddress ownerIP = namingService.findOwner(hash);
            logger.info("filename: " + fileName + " gave hash " + hash + " and returned owner " + ownerIP);
            replicationMap.put(fileName, ownerIP);

        }

        return new ResponseEntity<>(replicationMap, HttpStatus.OK);
    }
}
