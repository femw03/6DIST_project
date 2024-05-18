package origin.project.client.repository;

import org.springframework.data.repository.CrudRepository;
import origin.project.client.model.dto.LogEntry;

import java.net.InetAddress;
import java.util.List;


public interface LogRepository extends CrudRepository<LogEntry, Integer> {
    public List<LogEntry> findAllByDownloadLocationID(InetAddress downloadLocationID);

    public List<LogEntry> findAllByOwnerNodeID(InetAddress ownerID);
}
