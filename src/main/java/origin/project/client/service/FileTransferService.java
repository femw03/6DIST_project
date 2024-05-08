package origin.project.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.service.filelogs.FileLogEntry;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

@Service
public class FileTransferService {
    @Autowired
    private Node node;

    @Autowired
    private MessageService messageService;

    Logger logger = Logger.getLogger(FileTransferService.class.getName());

    public FileTransferService() {
        new Thread(this::receiveFile).start();
    }

    public void sendFile(String fileName, InetAddress nodeIP) {
        logger.info("Sending file log...");
        sendFileLog(fileName, nodeIP);

        logger.info("Sending file data...");
        sendFileData(fileName, nodeIP);
    }

    public void receiveFile() {
        while (true) {
            FileLogEntry fileLogEntry = receiveFileLog();
            if (fileLogEntry != null) {
                String fileName = fileLogEntry.getFileName();
                logger.info("Received file log from " + fileName);
                receiveFileData(fileName);
                logger.info("Received file data");
            }
        }
    }

    private void sendFileData(String fileName, InetAddress nodeIP) {
        try {
            // Create a socket object
            Socket socket  = new Socket();
            socket.connect(new InetSocketAddress(nodeIP, node.getNodePort()));
            OutputStream os = socket.getOutputStream();

            // Read the file and create an inputStream
            FileInputStream fileInputStream = new FileInputStream(fileName); // To read content of file in bytes format

            // Write teh files in bytes
            byte[] byteArray = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(byteArray)) > 0) {
                os.write(byteArray, 0, bytesRead);
            }
            // Flush and close output stream
            os.flush();
            os.close();

            // Close input and socket stream
            fileInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFileLog(String fileName, InetAddress nodeIP) {
        try {
            // Create a socket object
            Socket socket  = new Socket(nodeIP, node.getNodePort());
            OutputStream os = socket.getOutputStream();

            // Read the fileEntry and transform it to a jsonFile
            FileLogEntry fileLogEntry = node.getFileLogRepository().findByFileName(fileName);
            String jsonStr = fileLogEntry.toString();

            // Write the files in bytes
            PrintWriter writer = new PrintWriter(os, true);
            writer.println(jsonStr);

            // Close the socket
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFileData(String fileName) {
        try {
            // Create a server socket to receive the file
            ServerSocket serverSocket = new ServerSocket(node.getNodePort());
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();

            // Create a file where all the data will be written to
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            byte[] byteArray = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(byteArray)) > 0) {
                fileOutputStream.write(byteArray, 0, bytesRead);
            }

            // Flush and close output stream
            fileOutputStream.flush();
            fileOutputStream.close();

            // Close input and socket stream
            inputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileLogEntry receiveFileLog() {
        try {
            // We open a new socket
            ServerSocket serverSocket = new ServerSocket(node.getNodePort());
            Socket socket = serverSocket.accept();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            StringBuilder jsonStr = new StringBuilder();
            while ((line = reader.readLine())!= null) {
                jsonStr.append(line);
            }

            FileLogEntry fileLogEntry = new FileLogEntry(jsonStr.toString());

            // We close the sockets
            socket.close();
            serverSocket.close();

            return fileLogEntry;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
