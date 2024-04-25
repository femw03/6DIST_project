package origin.project.server.model.naming.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;

@Getter
@Setter
@ToString
public class NodeRequest {
    String name;
    InetAddress ip;
    int hash = -1;

    public NodeRequest(String name, InetAddress ip) {
        this.name = name;
        this.ip = ip;
    }

}