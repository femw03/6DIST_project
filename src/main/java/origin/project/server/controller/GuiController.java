package origin.project.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import origin.project.client.Node;
import origin.project.client.service.MessageService;
import origin.project.server.repository.NamingRepository;
import origin.project.server.service.NamingService;

import java.util.logging.Logger;

@Controller
public class GuiController {
    @GetMapping("/")
    public String dashboard(Model model) {
        // Add necessary data to the model
        model.addAttribute("dashboardContent", "Dashboard content goes here...");
        // Add more data as needed
        model.addAttribute("namingServerIp", "173.18.0.3"); // test, need function to get namingserver IP???
        model.addAttribute("existingNodes", 5);
        // Return the view name (HTML template name)
        return "GUI";
    }
}
