package origin.project.client.service.filelogs;

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
@Table(name = "filelog-entries")
public class FileLogEntry {
    @Id
    private String fileName;

    @Column
    private String fileHistory;
    public FileLogEntry(String fileName) {
        this.fileName = fileName;
    }

    public FileLogEntry() {}
}
