package origin.project.naming.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import origin.project.naming.controller.NamingServerController;
import origin.project.naming.model.naming.NamingEntry;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class JsonService {
    Logger logger = Logger.getLogger(NamingServerController.class.getName());

    // reads json-file of objects into an array which is then cast to a list.
    // https://howtodoinjava.com/gson/gson-parse-json-array/
    public List<NamingEntry> loadNamingEntriesFromJsonFile(String filePath) {
        try {
            FileReader reader = new FileReader(filePath);

            Gson gson = new Gson();

            NamingEntry[] objects = gson.fromJson(reader, NamingEntry[].class);

            // Close the reader
            reader.close();

            // Convert array to list
            return List.of(objects);
            // Convert array to list
        } catch (IOException e) {
            logger.info("failed to load entries from JSON-file. file: " + filePath);
            return null;
        }
    }

    public void addEntryToJsonFile(String filePath, NamingEntry entry) {
        try {
            // first read the array
            FileReader reader = new FileReader(filePath);
            Gson gson = new Gson();
            NamingEntry[] objects = gson.fromJson(reader, NamingEntry[].class);

            // append new entry
            List<NamingEntry> originalList = new ArrayList<>(List.of(objects));
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

    public void removeEntryFromJsonFile(String filePath, NamingEntry entry) {
        try {
            // first read the array
            FileReader reader = new FileReader(filePath);
            Gson gson = new Gson();
            NamingEntry[] objects = gson.fromJson(reader, NamingEntry[].class);

            // append new entry
            List<NamingEntry> originalList = new ArrayList<>(List.of(objects));
            originalList.removeIf(e -> Objects.equals(e.getHash(), entry.getHash()) && Objects.equals(e.getIP(), entry.getIP()));

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
