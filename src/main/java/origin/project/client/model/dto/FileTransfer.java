package origin.project.client.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileTransfer {
    private String fileName;
    private byte[] file;
    private InetAddress downloadLocation;

}
