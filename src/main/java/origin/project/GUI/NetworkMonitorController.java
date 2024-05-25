package origin.project.GUI;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NetworkMonitorController {

    @GetMapping("/")
    public String dashboard(Model model) {
        // Add necessary data to the model
        model.addAttribute("dashboardContent", "Dashboard content goes here...");
        // Add more data as needed

        // Return the view name (HTML template name)
        return "dashboard";
    }
}
