package origin.project.client.service.filelogs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FileLogRepository extends CrudRepository<FileLogEntry, Integer> {

    FileLogEntry findByFileName(String fileName);
}
