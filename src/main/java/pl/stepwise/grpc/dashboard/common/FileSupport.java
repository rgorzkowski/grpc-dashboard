package pl.stepwise.grpc.dashboard.common;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Created by rafal on 6/7/17.
 */
public class FileSupport {

    public static File getFileFromClassPath(String path) throws URISyntaxException {
        return Paths.get(FileSupport.class.getClassLoader().getResource(path).toURI())
                .toFile();
    }
}
