package origin.project.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

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

    @Value("${localfiles.path}")
    private String LOCAL_FILES_PATH;

    @Value("${replicatedfiles.path}")
    private String REPLICATED_FILES_PATH;

    private int currentID;
    private int nextID=-1;
    private int previousID=-1;
    private boolean discoveryFinished = false;
    private int existingNodes=0;
    private boolean pingEnable=false;
    private Map<String, InetAddress> replicatedLog;     // <fileName, downloadLocation>
    private Map<String, InetAddress> localLog;          // <fileName, replicatedNode>

    public InetAddress getIpAddress() throws UnknownHostException {
        return InetAddress.getByName(ipAddress);
    }

}