package origin.project.client.repository;

import org.springframework.data.repository.CrudRepository;
import origin.project.client.model.dto.LogEntry;

import java.net.InetAddress;
import java.util.List;


public interface LogRepository extends CrudRepository<LogEntry, Integer> {
    List<LogEntry> findAllByDownloadLocationID(InetAddress downloadLocationID);

    List<LogEntry> findAllByOwnerNodeID(InetAddress ownerID);
}
