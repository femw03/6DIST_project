package origin.project.client.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileTransfer {
    private String fileName;
    private byte[] file;
    private LogEntry logEntry;
}
