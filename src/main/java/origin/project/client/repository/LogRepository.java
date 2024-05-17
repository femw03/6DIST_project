package origin.project.client.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import origin.project.client.model.dto.LogEntry;
import origin.project.server.model.naming.NamingEntry;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

@Repository
public interface LogRepository extends CrudRepository<LogEntry, Integer> {
    public List<LogEntry> findAllByDownloadLocationID(InetAddress downloadLocationID);

    public List<LogEntry> findAllByOwnerNodeID(InetAddress ownerID);
}

