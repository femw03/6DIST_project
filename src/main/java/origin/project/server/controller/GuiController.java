package origin.project.server.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Getter
@Setter
@Controller
@RequestMapping("/")
public class GuiController {
    @GetMapping("/")
    public String dashboard(Model model) {
        // Add necessary data to the model
        model.addAttribute("dashboardContent", "Dashboard content goes here...");
        // Add more data as needed

        // Return the view name (HTML template name)
        return "GUI";
    }
}
