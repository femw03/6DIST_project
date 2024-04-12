package origin.project.client.service;

import org.springframework.web.client.RestTemplate;
import origin.project.client.Node;

public class HashService {
    public static int calculateHashFromNamingServer(String nodeName, String namingServerUrl) {
        int hash = -1;
        RestTemplate restTemplate = new RestTemplate();
        String url = namingServerUrl + "/get-hash/" + nodeName;
        return restTemplate.getForObject(url, Integer.class);
    }
}
