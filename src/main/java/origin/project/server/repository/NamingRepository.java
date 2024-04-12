package origin.project.server.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import origin.project.server.model.naming.NamingEntry;

import java.util.List;
import java.util.Optional;

@Repository
public interface NamingRepository extends CrudRepository<NamingEntry, Integer> {
    boolean existsByIP(String Ip);

    List<NamingEntry> findByHashLessThan(int hash);

    long count();

    @Query("SELECT e FROM NamingEntry e WHERE e.hash = (SELECT MAX(ee.hash) FROM NamingEntry ee)")
    Optional<NamingEntry> findEntryWithLargestHash();

}