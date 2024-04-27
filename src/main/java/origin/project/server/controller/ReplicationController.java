package origin.project.server.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import origin.project.server.service.NamingService;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RestController
@RequestMapping("/replication")
public class ReplicationController {
    @Autowired
    NamingService namingService;

    @PostMapping("/initial-list")
    public Map<String, InetAddress> nodeReportsInitialFiles(@RequestBody String hashedFileNamesJSON) {
        Map<String, InetAddress> replicationMap = new HashMap<>();

        Gson gson = new Gson();

        Type type = new TypeToken<HashMap<String, Integer>>() {}.getType();

        HashMap<String, Integer> map = gson.fromJson(hashedFileNamesJSON, type);

        for (String key : map.keySet()) {
            System.out.println(key + " " + String.valueOf(map.get(key)));
            InetAddress ownerIP = namingService.findOwner(map.get(key));
            System.out.println(ownerIP);
            replicationMap.put(key, ownerIP);

        }

        return replicationMap;
    }
}
