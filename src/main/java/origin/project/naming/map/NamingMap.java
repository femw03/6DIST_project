/*
* https://www.geeksforgeeks.org/map-interface-java-examples/
* */

package origin.project.naming.map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter

@Component
public class NamingMap extends HashMap<Integer,String>{}
