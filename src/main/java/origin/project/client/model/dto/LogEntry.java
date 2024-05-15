package origin.project.client.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;

@Getter
@Setter
@ToString
public class LogEntry {
    private String fileName;
    private InetAddress ownerNodeID;
    private InetAddress downloadLocationID;

    public LogEntry(String fileName, InetAddress ownerNodeID, InetAddress downloadLocationID) {
        this.fileName = fileName;
        this.ownerNodeID = ownerNodeID;
        this.downloadLocationID = downloadLocationID;
    }
}
