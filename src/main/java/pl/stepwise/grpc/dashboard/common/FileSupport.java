package pl.stepwise.grpc.dashboard.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

/**
 * Created by rafal on 6/7/17.
 */
public class FileSupport {

    public static File getFileFromClassPath(String path) throws URISyntaxException, IOException {
        InputStream in = FileSupport.class.getClassLoader().getResourceAsStream(path);
        File tmpFile = File.createTempFile(path, null);
        tmpFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tmpFile);
        IOUtils.copy(in, out);
        return tmpFile;
    }
}
