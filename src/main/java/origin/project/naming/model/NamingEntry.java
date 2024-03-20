package origin.project.naming.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "naming-entries")
public class NamingEntry {
    @Id
    private Integer hash;

    private String IP;

    public NamingEntry(Integer hash, String IP) {
        this.hash = hash;
        this.IP = IP;
    }

    public NamingEntry() {}
}
