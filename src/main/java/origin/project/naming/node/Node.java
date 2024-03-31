package origin.project.naming.node;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import origin.project.naming.service.NamingService;

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