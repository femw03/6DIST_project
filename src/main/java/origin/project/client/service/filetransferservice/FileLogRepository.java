package origin.project.client.service.filetransferservice;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import origin.project.server.model.naming.NamingEntry;

import java.util.List;


@Repository
public interface FileLogRepository extends CrudRepository<FileLogEntry, Integer> {

    FileLogEntry findByFileName(String fileName);
}
