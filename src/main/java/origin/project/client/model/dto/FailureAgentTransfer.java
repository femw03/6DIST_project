package origin.project.client.model.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FailureAgentTransfer {
    private String IPFailingNode;
    private int IDStartingNode;
}
