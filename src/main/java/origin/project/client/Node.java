package origin.project.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import origin.project.client.service.filelogs.FileLogRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Setter
@Component
public class Node {

    @Autowired
    private FileLogRepository fileLogRepository;

    @Value("4444")
    private int fileTransferPort;

    @Value("5555")
    private int fileLogTransferPort;

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

    private int currentID;
    private int nextID=-1;
    private int previousID=-1;

    @Value("${nodeName}")
    private String nodeName;

    @Value("${ipAddress}")
    private String ipAddress;

    @Value("${node.filepath}")
    private String FILE_PATH;
    @Value("${node.directory}")
    private String DIRECTORY;

    public InetAddress getIpAddress() throws UnknownHostException {
        return InetAddress.getByName(ipAddress);
    }
}