package origin.project.client.model.dto;

import origin.project.client.Node;

import java.io.File;

public class LogEntry {
    private File file;
    private int ownerNodeID;
    private int downloadLocationID;

    public LogEntry(File file, int ownerNodeID, int downloadLocationID) {
        this.file = file;
        this.ownerNodeID = ownerNodeID;
        this.downloadLocationID = downloadLocationID;
    }

    @Override
    public String toString() {
        return "File: " + file.toString() + " with owner of file " + ownerNodeID + " and download location " + downloadLocationID;
    }
}