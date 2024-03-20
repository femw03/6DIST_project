package origin.project.naming.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import origin.project.naming.controller.NamingServerController;
import origin.project.naming.repository.NamingRepository;

import java.io.Serializable;

@Component
public class NamingServer implements Serializable {
    // class implementation
    @Autowired
    NamingRepository namingRepository;

    @Autowired
    NamingServerController namingServerController;
}