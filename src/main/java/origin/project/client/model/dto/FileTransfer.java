package origin.project.client.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileTransfer {
    private String fileName;
    private byte[] file;
    private LogEntry log;

}
