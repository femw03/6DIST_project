package origin.project.naming.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import origin.project.naming.node.Node;
import origin.project.naming.service.NamingService;

@Getter
@Setter
@Entity
@Table(name = "namingEntries")
public class NamingEntry {
    @Id
    private Integer nodeId;
    private String ipAddress;
    public NamingEntry() {
    }

    public NamingEntry(Node node) {
        this.ipAddress = node.getIpAddress();
        this.nodeId = NamingService.hashingFunction(node.getName());
    }
}