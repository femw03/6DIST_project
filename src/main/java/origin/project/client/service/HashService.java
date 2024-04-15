package origin.project.client.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HashService {
    public static int calculateHashFromNamingServer(String nodeName, String namingServerUrl) {
        RestTemplate restTemplate = new RestTemplate();
        String url = namingServerUrl + "/get-hash/" + nodeName;
        return restTemplate.getForObject(url, Integer.class);
    }
}
