package origin.project.GUI;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import origin.project.client.service.MessageService;

import java.util.logging.Logger;

@Controller
public class NetworkMonitorController {
    Logger logger = Logger.getLogger(MessageService.class.getName());

    @GetMapping("/")
    public String dashboard(Model model) {
        // Add necessary data to the model
        model.addAttribute("dashboardContent", "Dashboard content goes here...");
        // Add more data as needed
        model.addAttribute("namingServerIp", "192.168.1.1");
        // Return the view name (HTML template name)
        return "dashboard2";
    }
}
