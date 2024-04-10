package origin.project.naming.model.naming;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "naming-entries")
public class NamingEntry {
    @Id
    private Integer hash;

    @Column(nullable = false)
    private String IP;

    public NamingEntry(Integer hash, String IP) {
        this.hash = hash;
        this.IP = IP;
    }

    public NamingEntry() {}
}
