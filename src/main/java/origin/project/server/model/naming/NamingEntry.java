package origin.project.server.model.naming;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;

@Getter
@Setter
@ToString
@Entity
@Table(name = "naming-entries")
public class NamingEntry {
    @Id
    private Integer hash;

    @Column(nullable = false)
    private InetAddress IP;

    public NamingEntry(Integer hash, InetAddress IP) {
        this.hash = hash;
        this.IP = IP;
    }

    public NamingEntry() {}
}