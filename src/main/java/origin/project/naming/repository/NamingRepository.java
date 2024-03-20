package origin.project.naming.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NamingRepository extends CrudRepository<String, Integer> {

}
