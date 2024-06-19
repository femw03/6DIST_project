package origin.project.client.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FailureAgentTransfer {
    private String IPFailingNode;
    private int IDStartingNode;
}
