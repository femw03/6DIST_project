package origin.project.server.model.naming.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NodeRequest {
    String name;
    String ip;
    int hash = -1;

    public NodeRequest(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

}