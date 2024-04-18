package origin.project.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Setter
@Component
public class Node {

    @Value("${multicast.port}")
    private int multicastPort;

    @Value("${multicast.group}")
    private String multicastGroup;

    @Value("${naming.server.base.url}")
    private String namingServerUrl;

    @Value("${naming.server.ip}")
    private String namingServerIp;

    private int currentID;
    private int nextID=-1;
    private int previousID=-1;

    @Value("${nodeName}")
    private String nodeName;

    @Value("${ipAddress}")
    private String ipAddress;

    public InetAddress getIpAddress() throws UnknownHostException {
        return InetAddress.getByName(ipAddress);
    }
}