package com.filekeeper.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Fileinfo {
    public enum FileType{
        File("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    private String filename;
    private FileType type;
    private long size;
    private LocalDateTime lastModified;
    private String dir;

    public String getFilename() {
        return filename;
    }

    public FileType getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getDir() {
        return dir;
    }

    public Fileinfo(Path path){
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.File;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(0));
        } catch (IOException e) {
            throw new RuntimeException("Can't get file info");
        }
    }

    public Fileinfo(String dir, String filename, FileType type, long size, LocalDateTime lastModified) {
        this.dir = dir;
        this.filename = filename;
        this.type = type;
        this.size = size;
        this.lastModified = lastModified;
    }

    public static List<Fileinfo> getFileInfoList(List<String> files) {
//        DateTimeFormatter aFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
//        LocalDateTime localDateTime = LocalDateTime.of(2017, Month.AUGUST, 3, 12, 30, 25);

        List<Fileinfo> fileinfos = new ArrayList<>();

        String[] sp;
        for (String s : files) {
            sp = s.split(" ");
            fileinfos.add(new Fileinfo(sp[0], sp[1], getType(sp[1]), Long.valueOf(sp[2]), getLocalDateTime(sp[3], sp[4])));
        }
        return fileinfos;
    }

    public static FileType getType(String s) {
        if(s.indexOf(".") > -1) {
            return FileType.File;
        } else {
            return FileType.DIRECTORY;
        }

    }

    private static LocalDateTime getLocalDateTime(String s1, String s2) {
        int year = Integer.parseInt(s1.split("-")[2]);
        Month month = Month.of(Integer.parseInt(s1.split("-")[0]));
        int day = Integer.parseInt(s1.split("-")[0]);

        int hours = Integer.parseInt(s2.split(":")[0]);
        int minute = Integer.parseInt(s2.split(":")[1]);
        int seconds = Integer.parseInt(s2.split(":")[2]);

        return LocalDateTime.of(year, month, day, hours, minute, seconds);
    }
}
