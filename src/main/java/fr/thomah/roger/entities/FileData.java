package fr.thomah.roger.entities;

public class FileData {

    public String id;

    public String fileName;

    public String matches = "";

    public boolean isSync = false;

    public FileData() {}

    public FileData(String fileName) {
        this.fileName = fileName;
    }

}
