package origin.project.client;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import origin.project.client.agents.SyncAgent;
import origin.project.client.model.dto.LogEntry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
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
    private boolean newNode = false;
    private Map<String, LogEntry> log;

    Logger logger = Logger.getLogger(Node.class.getName());

    @PostConstruct
    public void init() throws StaleProxyException {
        // create the main-container.
        // This is the central container responsible for managing agents and other containers.
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        AgentContainer mainContainer = rt.createMainContainer(profile);
        // create a SyncAgent in the container.
        AgentController controller = mainContainer.createNewAgent(nodeName, SyncAgent.class.getName(), new Object[] {this});
        // start the SyncAgent.
        controller.start();
    }
    public InetAddress getIpAddress() throws UnknownHostException {
        return InetAddress.getByName(ipAddress);
    }

}