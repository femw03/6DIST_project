package origin.project.naming.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeRepository extends CrudRepository<NamingEntry,Integer> {
    Optional<NamingEntry> findByIpAddress(String ip);
}