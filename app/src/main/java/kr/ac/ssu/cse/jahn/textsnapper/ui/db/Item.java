package kr.ac.ssu.cse.jahn.textsnapper.ui.db;

/**
 * Created by ArchSlave on 2017-12-07.
 */

public class Item implements Comparable<Item> {
    private String fileName;
    private String fileSize;
    private String fileDate;
    private String filePath;

    public Item(String name, String size, String date, String path) {
        fileName = name;
        fileSize = size;
        fileDate = date;
        filePath = path;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFileDate() {
        return fileDate;
    }

    public String getFilePath() { return filePath; }

    @Override
    public int compareTo(Item item) {
        if(fileName != null) {
            return fileDate.compareTo(item.getFileDate());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
