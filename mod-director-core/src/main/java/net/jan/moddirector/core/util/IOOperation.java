package net.jan.moddirector.core.util;

import net.jan.moddirector.core.manage.ProgressCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOOperation {
    public static void copy(InputStream inputStream, OutputStream outputStream, ProgressCallback callback,
                            long knownLength)
            throws IOException {
        callback.indeterminate(knownLength < 0);

        byte[] buffer = new byte[1024];

        long progress = 0;
        int read;
        while((read = inputStream.read(buffer)) > 0) {
            progress += read;
            callback.reportProgress(progress, knownLength);
            outputStream.write(buffer, 0, read);
        }

        inputStream.close();
        outputStream.close();
    }
}
