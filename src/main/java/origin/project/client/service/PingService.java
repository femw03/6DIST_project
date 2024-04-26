package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;

import java.io.IOException;
import java.net.*;
import java.util.Objects;
import java.util.logging.Logger;


@Service
public class PingService {
    @Autowired
    private Node node;
    @Autowired
    private MessageService messageService;
    private int PORT;
    private String MULTICAST_GROUP;
    private InetAddress IPnext;
    private InetAddress IPprevious;
    //private String namingServerUrl;
    private MulticastSocket socket;
    static Logger logger = Logger.getLogger(PingService.class.getName());

    public PingService(Node node) throws IOException {
        this.node = node;
        this.PORT = node.getMulticastPort();
        this.MULTICAST_GROUP = node.getMulticastGroup();
        //this.namingServerUrl = node.getNamingServerUrl();
        //this.messageService = messageService;

        /*while (!initiated) { // Test!!!
            if (node.getExistingNodes() > 1) {
                initiated = true;
                logger.info("I am here!!!");
                new Thread(this::Ping).start();
            }
        }*/
        new Thread(this::Ping).start();
    }

    public void Ping() {
        while(true){
            try {
                Thread.sleep(5000); // 5000 milliseconds = 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (node.getExistingNodes() > 1) {
                    try {
                        logger.info("I am here (PING PING)!!!");
                        int nextID = node.getNextID();
                        int previousID = node.getPreviousID();
                        int myID = node.getCurrentID();

                        // ???
                        if (nextID != -1 && previousID != -1 && myID != -1) {
                            // next
                            String URLnext = node.getNamingServerUrl() + "/get-IP-by-hash/" + nextID;
                            String IPnext = messageService.getRequest(URLnext, "get next ip");
                            IPnext = IPnext.replace("\"", "");              // remove double quotes
                            InetAddress IPnextInet =  InetAddress.getByName(IPnext);

                            // previous
                            String URLprevious = node.getNamingServerUrl() + "/get-IP-by-hash/" + previousID;
                            String IPprevious = messageService.getRequest(URLprevious, "get previous ip");
                            IPprevious = IPprevious.replace("\"", "");              // remove double quotes
                            InetAddress IPpreviousInet =  InetAddress.getByName(IPprevious);

                            logger.info("url request ping : " + node.getNamingServerUrl() + "/get-IP-by-hash/" + nextID);

                            //send
                            sendPing(IPnext);
                            sendPing(IPprevious);
                        } else {
                            logger.info("nextID: " + nextID + " previousID: " + previousID + " myID: " + myID);
                            // Find way to make him wait on response from server!!!
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                //}
            }
        }
    }

    /*
    * java.io.IOException: Invalid multicast message format
        at origin.project.client.service.MessageService.processUnicastMessage(MessageService.java:120)
        at origin.project.client.service.MessageService.processMessage(MessageService.java:87)
        at origin.project.client.service.MessageService.receiveMessage(MessageService.java:68)
        at java.base/java.lang.Thread.run(Thread.java:833)
        *
        * ???
    * */
    private void sendPing(String receiverIP) throws UnknownHostException {
        try (DatagramSocket socket = new DatagramSocket()) {
            System.out.println("IP address in sendPing method: " + receiverIP);
            InetAddress receiverAddress = InetAddress.getByName(receiverIP);
            String responseMessage = "hello";
            byte[] buf = responseMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, receiverAddress, PORT);
            socket.send(packet);
        } catch (IOException e) {
            try {
                new FailureService().Failure(node,messageService); //???
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
        }
    }
}
