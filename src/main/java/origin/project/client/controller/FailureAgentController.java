package origin.project.client.controller;


import com.google.gson.Gson;
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
@RequestMapping("/failure-agent")
public class FailureAgentController {

    private Logger logger = Logger.getLogger(FailureAgentController.class.getName());

    @Autowired
    private Node node;

    @Autowired
    private MessageService messageService;

    @PostMapping("/run-failure-agent")
    public ResponseEntity<String> previousNodeSendsFailureAgent(@RequestBody String failureAgentTransferJSON) throws StaleProxyException {
        Gson gson = new Gson();
        FailureAgentTransfer failureAgentTransfer = gson.fromJson(failureAgentTransferJSON, FailureAgentTransfer.class);
        logger.info("Received " + failureAgentTransfer);

        String IPFailingNode = failureAgentTransfer.getIPFailingNode();
        int IDStartingNode = failureAgentTransfer.getIDStartingNode();

        if (IPFailingNode == null || IPFailingNode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP of Failing node");
        }

        // create a FailureAgent in the container.
        String agentName = node.getNodeName() + " FailureAgent(" + IPFailingNode + ", " + IDStartingNode + ")";
        AgentController controller = node.getMainContainer().createNewAgent(agentName, FailureAgent.class.getName(), new Object[] {IPFailingNode, IDStartingNode, node, messageService});
        // start the FailureAgent.
        controller.start();
        return ResponseEntity.ok("Failure agent received successfully.");
    }
}
