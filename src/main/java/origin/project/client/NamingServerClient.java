package origin.project.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class NamingServerClient {
    private String serverBaseUrl;
    private String emailClient;

    private int clientId;

    public NamingServerClient(String hostnameServer, int portServer) {
        // base-url of server. e.g., localhost:8080
        this.serverBaseUrl = "http://" + hostnameServer + ":" + portServer;

        run();
    }

    private void run() {

        // Add some nodes
        System.out.print(addNode("node1", "192.168.1.1"));
        System.out.print(addNode("node2", "192.168.1.2"));
        System.out.print(addNode("node3", "192.168.1.3"));
        System.out.print(addNode("node4", "192.168.1.4"));

        // print all the nodes
        System.out.print(getAllNodes());

        // remove some nodes
        System.out.print(removeNode("192.168.1.4"));

        // print all the nodes
        System.out.print(getAllNodes());

        // get a loction fo a file
        System.out.print(getFileLocation("document1.txt"));

        // clear the repository
        System.out.print(clearRepository());

    }


    private String addNode(String nodeName, String ipAddress) {
        String nodeEndpoint = serverBaseUrl + "/addNode";
        String nodeBody = "{\"nodeName\" : \"" + nodeName + "\", \"ipAddress\" : \"" + ipAddress + "\"}" ;
        return postRequest(nodeEndpoint, nodeBody, "addNode");
    }

    private String removeNode(String ipAddress) {
        String nodeEndpoint = serverBaseUrl + "/removeNode";
        String nodeBody = "{\"ipAddress\" : \"" + ipAddress + "\"}" ;
        return postRequest(nodeEndpoint, nodeBody, "removeNode");
    }

    private String getNode(String ipAddress) {
        String nodeEndpoint = serverBaseUrl + "/getNode";
        String nodeBody = "{\"ipAddress\" : \"" + ipAddress + "\"}" ;
        return getRequest(nodeEndpoint, nodeBody, "getNode");
    }


    private String getAllNodes() {
        String nodeEndpoint = serverBaseUrl + "/getAllNodes";
        String nodeBody = "{}" ;
        return getRequest(nodeEndpoint, nodeBody, "getAllNodes");
    }

    private String getFileLocation(String fileName) {
        String nodeEndpoint = serverBaseUrl + "/getFileLocation";
        String nodeBody = "{\"fileName\" : \"" + fileName + "\"}" ;
        return getRequest(nodeEndpoint, nodeBody, "getFileLocation");
    }

    private String clearRepository() {
        String nodeEndpoint = serverBaseUrl + "/clearRepository";
        String nodeBody = "{}" ;
        return deleteRequest(nodeEndpoint, nodeBody, "clearRepository");
    }


    //https://www.baeldung.com/java-http-request
    private String getRequest(String endpoint, String requestbody, String request) {
        try {
            URL url = new URL(endpoint);
//            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Try writing the email to JSON
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                byte[] requestBody = requestbody.getBytes(StandardCharsets.UTF_8);
                outputStream.write(requestBody, 0, requestBody.length);
            }


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
                System.out.println("Failed to " + request + ". HTTP Error: " + connection.getResponseCode());
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
                return response.toString();
            }
            else {
                // If the request was not successful, handle the error accordingly
                System.out.println("Failed to " + request + ". HTTP Error: " + connection.getResponseCode());
            }
            connection.disconnect();
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String deleteRequest(String endpoint, String requestbody, String request) {

        try {
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
                return response.toString();
            }
            else {
                // If the request was not successful, handle the error accordingly
                System.out.println("Failed to " + request + ". HTTP Error: " + connection.getResponseCode());
            }
            connection.disconnect();
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
