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