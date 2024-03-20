package origin.project.naming;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.naming.model.NamingEntry;
import origin.project.naming.repository.NamingRepository;

@Service
public class DatabaseLoader {
    @Autowired
    final private NamingRepository namingRepository;

    public DatabaseLoader(NamingRepository namingRepository) {
        this.namingRepository = namingRepository;
    }

    @PostConstruct
    private void initDatabase() {
        NamingEntry localhost = new NamingEntry(1, "localhost");
        namingRepository.save(localhost);
    }
}
