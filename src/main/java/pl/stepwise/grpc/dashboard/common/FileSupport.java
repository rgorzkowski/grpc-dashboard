package pl.stepwise.grpc.dashboard.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<byte[]> split(byte[] photo, int blockSize) {
        List<byte[]> result = new ArrayList<>();
        int fullBlockCount = photo.length / blockSize;

        for (int i = 0; i < fullBlockCount; i++) {
            int startIdx = i * blockSize;
            result.add(Arrays.copyOfRange(photo, startIdx, startIdx + blockSize));
        }
        if (photo.length % blockSize > 0) {
            int startIdx = fullBlockCount * blockSize;
            result.add(Arrays.copyOfRange(photo, startIdx, startIdx + photo.length % blockSize));
        }
        return result;
    }
}
