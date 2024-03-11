package origin.project.naming.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import origin.project.naming.map.NamingMap;

@RestController
public class MapController {
    @Autowired
    NamingMap map;

    //@PostMapping("/add")

}
