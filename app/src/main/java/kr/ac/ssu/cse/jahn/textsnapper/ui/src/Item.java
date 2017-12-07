package kr.ac.ssu.cse.jahn.textsnapper.ui.src;

/**
 * Created by ArchSlave on 2017-12-07.
 */

public class Item implements Comparable<Item> {
    private String fileName;
    private String fileSize;
    private String fileDate;

    public Item(String name, String size, String date) {
        fileName = name;
        fileSize = size;
        fileDate = date;
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    @Override
    public int compareTo(Item item) {
        if(fileName != null) {
            return fileName.toLowerCase().compareTo(item.getFileName().toLowerCase());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
