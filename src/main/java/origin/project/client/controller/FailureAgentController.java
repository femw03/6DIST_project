package origin.project.client.controller;


import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import origin.project.client.Node;
import origin.project.client.agents.FailureAgent;
import origin.project.client.model.dto.FailureAgentTransfer;
import origin.project.client.service.MessageService;

import java.util.logging.Logger;

@Getter
@Setter
@RestController
@RequestMapping("/failure")
public class FailureAgentController {

    private Logger logger = Logger.getLogger(FailureAgentController.class.getName());

    private Node node;

    @Autowired
    private MessageService messageService;

    @PostMapping("/failure-agent")
    public ResponseEntity<String> previousNodeSendsFailureAgent(@RequestBody FailureAgentTransfer failureAgentTransfer) throws StaleProxyException {
        String IPFailingNode = failureAgentTransfer.getIPFailingNode();
        int IDStartingNode = failureAgentTransfer.getIDStartingNode();

        if (IPFailingNode == null || IPFailingNode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Ip of Failing node");
        }

        // create a FailureAgent in the container.
        AgentController controller = node.getMainContainer().createNewAgent(node.getNodeName() + "failure agent", FailureAgent.class.getName(), new Object[] {IPFailingNode, IDStartingNode, node, messageService});
        // start the FailureAgent.
        controller.start();
        return ResponseEntity.ok("Failure agent received successfully.");
    }
}
