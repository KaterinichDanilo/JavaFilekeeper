import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    public Fileinfo(String filename, FileType type, long size, LocalDateTime lastModified) {
        this.filename = filename;
        this.type = type;
        this.size = size;
        this.lastModified = lastModified;
    }

    public static FileType getType(String s) {
        if(s.equals("F")) {
            return FileType.File;
        } else {
            return FileType.DIRECTORY;
        }

    }
}
