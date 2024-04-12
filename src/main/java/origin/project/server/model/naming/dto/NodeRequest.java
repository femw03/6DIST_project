package origin.project.server.model.naming.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NodeRequest {
    String name;
    String ip;
    int hash = -1;


}