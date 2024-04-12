package origin.project.naming.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.naming.repository.NamingRepository;
@Service
public class DatabaseLoader {
    @Autowired
    final private NamingRepository namingRepository;

    @Autowired
    final private JsonService jsonService;

    private static final String FILE_PATH = "src/main/java/origin/project/naming/service/nodes.json";

    public DatabaseLoader(NamingRepository namingRepository, JsonService jsonService) {
        this.namingRepository = namingRepository;
        this.jsonService = jsonService;
    }

    @PostConstruct
    private void initDatabase() {
        namingRepository.saveAll(jsonService.loadNamingEntriesFromJsonFile(FILE_PATH));


//        NamingEntry localhost = new NamingEntry(1300, "localhost");
//        NamingEntry gones = new NamingEntry(200, "128.0.0.1");
//        NamingEntry femke = new NamingEntry(20000, "192.168.1.13");
//        NamingEntry nigel = new NamingEntry(32000, "192.168.2.56");
//        NamingEntry rien = new NamingEntry(28113, "192.168.1.42");
//        namingRepository.save(localhost);
//        namingRepository.save(gones);
//        namingRepository.save(femke);
//        namingRepository.save(nigel);
//        namingRepository.save(rien);
    }
}
