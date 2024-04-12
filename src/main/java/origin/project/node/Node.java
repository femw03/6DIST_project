package origin.project.node;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@Entity
//@Table(name = "nodeEntries")
public class Node {
    //@Id
    //private Integer nodeId;
    private String name;
    private String ipAddress;

    public Node() {
    }

    public Node(String name, String ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
        //this.nodeId = NamingService.hashingFunction(this.name);
    }
}