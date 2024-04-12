package origin.project.client;

import lombok.Getter;
import lombok.Setter;
import origin.project.client.multicast.MulticastService;

import java.net.InetAddress;

@Getter
@Setter
public class Node {
    private String name;
    private InetAddress ipAddress;
    private int currentID;
    private int nextID=-1;
    private int previousID=-1;

    public Node(String name, InetAddress ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
        bootstrap();
    }

    public void bootstrap() {
        MulticastService.sendMulticastMessage(this.name, this.ipAddress);
    }
}