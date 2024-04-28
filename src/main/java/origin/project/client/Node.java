package origin.project.client;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import origin.project.client.service.ShutdownService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

@Getter
@Setter
@Component
public class Node {
    // General configurations
    @Value("${multicast.port}")
    private int multicastPort;
    @Value("${multicast.group}")
    private String multicastGroup;
    @Value("${naming.server.base.url}")
    private String namingServerUrl;
    @Value("${naming.server.ip}")
    private InetAddress namingServerIp;
    @Value("${naming.server.port}")
    private int namingServerPort;
    @Value("${node.port}")
    private int nodePort;

    // Node configurations
    @Value("${nodeName}")
    private String nodeName;
    @Value("${ipAddress}")
    private String ipAddress;
    private int currentID;
    private int nextID=-1;
    private int previousID=-1;
    private int existingNodes=0;

    public InetAddress getIpAddress() throws UnknownHostException {
        return InetAddress.getByName(ipAddress);
    }

}