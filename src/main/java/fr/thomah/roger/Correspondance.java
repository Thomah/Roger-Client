package fr.thomah.roger;

public class Correspondance{
    private String key;
    private String fileName;

    Correspondance(String string, String string0) {
        key = string;
        fileName = string0;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    
}
