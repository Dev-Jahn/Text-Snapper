package kr.ac.ssu.cse.jahn.textsnapper.ui.db;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

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

    private Date getRawDate() {
        DateFormat formatter = DateFormat.getDateTimeInstance();
        try {
            return formatter.parse(fileDate);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getFileDate() {
        return fileDate;
    }

    public String getFilePath() { return filePath; }

    @Override
    public int compareTo(Item item) {
        if(fileName != null) {
            return getRawDate().compareTo(item.getRawDate()) * -1;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
