package origin.project.client.model.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class LogEntry {
    @Id
    @GeneratedValue
    private Long id;

    private String fileName;
    private InetAddress ownerNodeID;
    private InetAddress downloadLocationID;
    @Id
    @GeneratedValue
    private Long id;

    public LogEntry(String fileName, InetAddress ownerNodeID, InetAddress downloadLocationID) {
        this.fileName = fileName;
        this.ownerNodeID = ownerNodeID;
        this.downloadLocationID = downloadLocationID;
    }
}
