package origin.project.client.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import origin.project.client.model.dto.LogEntry;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;


public interface LogRepository extends CrudRepository<LogEntry, Integer> {
    List<LogEntry> findAllByDownloadLocationID(InetAddress downloadLocationID);

    List<LogEntry> findAllByOwnerNodeID(InetAddress ownerID);

    Optional<LogEntry> findByFileName(String fileName);

    @Transactional
    @Modifying
    void deleteByFileName(String fileName);

    boolean existsByFileName(String fileName);
}
