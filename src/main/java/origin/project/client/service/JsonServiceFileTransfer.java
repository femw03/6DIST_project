package origin.project.client.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import origin.project.client.Node;
import origin.project.client.service.filelogs.FileLogEntry;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class JsonServiceFileTransfer {
    Logger logger = Logger.getLogger(Node.class.getName());

    // reads json-file of objects into an array which is then cast to a list.
    // https://howtodoinjava.com/gson/gson-parse-json-array/
    public List<FileLogEntry> loadNamingEntriesFromJsonFile(String filePath) {
        try {
            FileReader reader = new FileReader(filePath);
            Gson gson = new Gson();

            FileLogEntry[] objects = gson.fromJson(reader, FileLogEntry[].class);

            // Close the reader
            reader.close();

            // Convert array to list
            if (objects == null)
                return null;

            return List.of(objects);

        } catch (IOException e) {
            logger.info("Failed to load entries from JSON-file. file: " + filePath);
            return null;
        }
    }

    public void addEntryToJsonFile(String filePath, FileLogEntry entry) {
        try {
            // first read the array
            FileReader reader = new FileReader(filePath);
            Gson gson = new Gson();
            FileLogEntry[] objects = gson.fromJson(reader, FileLogEntry[].class);

            // append new entry
            List<FileLogEntry> originalList = new ArrayList<>();
            if (objects != null) {
                originalList = new ArrayList<>(List.of(objects));

            }
            originalList.add(entry);

            // write new list to file
            gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter fileWriter = new FileWriter(filePath);
            gson.toJson(originalList, fileWriter);

            fileWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeEntryFromJsonFile(String filePath, FileLogEntry entry) {
        try {
            // first read the array
            FileReader reader = new FileReader(filePath);
            Gson gson = new Gson();
            FileLogEntry[] objects = gson.fromJson(reader, FileLogEntry[].class);

            // remove entry
            List<FileLogEntry> originalList = new ArrayList<>(List.of(objects));
            originalList.removeIf(e -> Objects.equals(e.getFileName(), entry.getFileName()));

            // write new list to file
            gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter fileWriter = new FileWriter(filePath);
            gson.toJson(originalList, fileWriter);

            fileWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearJsonFile(String filePath) {
        try {
            // first read the array
            FileReader reader = new FileReader(filePath);
            Gson gson = new Gson();
            FileLogEntry[] objects = gson.fromJson(reader, FileLogEntry[].class);

            // check if objects array is null or empty
            if (objects == null) {
                objects = new FileLogEntry[0];   // initialize empty array
            }

            // remove all entries by creating new empty list
            List<FileLogEntry> originalList = new ArrayList<>();

            // write new list to file
            gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter fileWriter = new FileWriter(filePath);
            gson.toJson(originalList, fileWriter);

            fileWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}