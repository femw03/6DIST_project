package origin.project;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SpringBootTest
class NamingServerTests {

    private String serverBaseUrl;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Syntax: provide parameter \"hostname server\" \"port server\"");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        new NamingServerTests(hostname, port);
    }

    public NamingServerTests(String hostnameServer, int portServer) {
        // base-url of server. e.g., localhost:8080
        this.serverBaseUrl = "http://" + hostnameServer + ":" + portServer + "/naming-server";

        run();
    }

    private void run() {
        // print all the nodes
        System.out.println("All nodes: "+getAllNodes());

        // Add nodes
        System.out.println(addNode("node2", "192.168.1.71"));
        System.out.println(addNode("node4", "192.168.1.51"));
        System.out.println(addNode("node6", "192.168.1.21"));

        // print all the nodes
        System.out.println("All nodes: "+getAllNodes());

        // get a loction fo a file
        System.out.println("Get location of file 'document1.txt': "+getFileLocation("document1.txt"));

        // remove some nodes
        removeNodeThroughName("192.168.1.71", "node2");
        System.out.println("Removed node2: "+getAllNodes());
        removeNodeThroughName("192.168.1.51", "node4");
        System.out.println("Removed node4: "+getAllNodes());
        removeNodeThroughHash("192.168.1.21", String.valueOf(4724));
        System.out.println("Removed node6: "+getAllNodes());
    }


    private String addNode(String nodeName, String ipAddress) {
        String addNodeEndpoint = serverBaseUrl + "/add-node";
        String addNodeBody = "{\"name\" : \"" + nodeName + "\", \"ip\" : \"" + ipAddress + "\"}" ;
        return postRequest(addNodeEndpoint, addNodeBody, "addNode");
    }

    private void removeNodeThroughName(String ipAddress, String name) {
        String nodeEndpoint = serverBaseUrl + "/remove-node";
        String nodeBody = "{\"name\" : \"" + name + "\", \"ip\" : \"" + ipAddress + "\"}" ;
        deleteRequest(nodeEndpoint, nodeBody, "removeNode");
    }

    private void removeNodeThroughHash(String ipAddress, String hash) {
        String nodeEndpoint = serverBaseUrl + "/remove-node";
        String nodeBody = "{\"hash\" : \"" + hash + "\", \"ip\" : \"" + ipAddress + "\"}" ;
        deleteRequest(nodeEndpoint, nodeBody, "removeNode");
    }

//    private String getNode(String ipAddress) {
//        String nodeEndpoint = serverBaseUrl + "/getNode";
//        String nodeBody = "{\"ipAddress\" : \"" + ipAddress + "\"}" ;
//        return getRequest(nodeEndpoint, "get node" + ipAddress);
//    }


    private String getAllNodes() {
        String nodeEndpoint = serverBaseUrl + "/all-nodes";
        return getRequest(nodeEndpoint, "get all nodes");
    }

    private String getFileLocation(String fileName) {
        String nodeEndpoint = serverBaseUrl + "/file-location/" + fileName;
        return getRequest(nodeEndpoint, "get file location" + fileName);
    }

//    private String clearRepository() {
//        String nodeEndpoint = serverBaseUrl + "/clearRepository";
//        String nodeBody = "{}" ;
//        return deleteRequest(nodeEndpoint, nodeBody, "clearRepository");
//    }


    //https://www.baeldung.com/java-http-request
    private String getRequest(String endpoint, String request) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // If the request successful (status code 200), we can read response.
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Reader reads the response from the input stream.
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // Builder is used to build the full response from the lines we read with the reader.
                StringBuilder response = new StringBuilder();
                // building the full response, including status messages, headers, ...
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                //
                return response.toString();
            } else {
                // If the request was not successful, handle the error accordingly
                System.out.println(request + "failed" + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (IOException e) {
            // Handling network-related errors
            e.printStackTrace();
        }
        return null;
    }

    private String postRequest(String endpoint, String requestbody, String request) {

        try {
            String output;

            URL url = new URL(endpoint);
//            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Try writing the email to JSON
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                byte[] requestBody = requestbody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(requestBody, 0, requestBody.length);
            }

            // If connection is successful, we can read the response
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // reader
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // response
                StringBuilder response = new StringBuilder();
                String line;
                // build response = adding status code, status message, headers, ...
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                output = response.toString();
            }
            else {
                // If the request was not successful, handle the error accordingly
                output = "Failed to " + request + ". HTTP Error: " + connection.getResponseCode();
            }
            connection.disconnect();
            return output;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String deleteRequest(String endpoint, String requestbody, String request) {

        try {
            String output;

            URL url = new URL(endpoint);
//            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Try writing the email to JSON
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                byte[] requestBody = requestbody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(requestBody, 0, requestBody.length);
            }

            // If connection is successful, we can read the response
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // reader
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                // response
                StringBuilder response = new StringBuilder();
                String line;
                // build response = adding status code, status message, headers, ...
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                output = response.toString();
            }
            else {
                // If the request was not successful, handle the error accordingly
                output = "Failed to " + request + ". HTTP Error: " + connection.getResponseCode();
            }
            connection.disconnect();
            return output;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
