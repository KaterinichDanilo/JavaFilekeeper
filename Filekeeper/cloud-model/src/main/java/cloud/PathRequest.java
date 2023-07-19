package cloud;

import java.nio.file.Path;

public class PathRequest implements CloudMessage{
    String path;

    public PathRequest(Path path) {
        this.path = String.valueOf(path);
    }

    public String getPath() {
        return path;
    }
}
