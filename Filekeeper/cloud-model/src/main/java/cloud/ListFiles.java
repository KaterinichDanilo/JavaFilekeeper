package cloud;

import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListFiles implements CloudMessage {

    private List<String> files = new ArrayList<>();

    public ListFiles(Path path) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        File[] file = new File(String.valueOf(path)).listFiles();
        String fileInfo;
        for (File f : file) {
            fileInfo = path.toString() + " " + f.getName() + " " + f.length() + " " + sdf.format(f.lastModified());
            files.add(fileInfo);
        }
    }

}
