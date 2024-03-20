package origin.project.naming.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import origin.project.naming.model.NamingEntry;

@Repository
public interface NamingRepository extends CrudRepository<NamingEntry, Integer> {

}
