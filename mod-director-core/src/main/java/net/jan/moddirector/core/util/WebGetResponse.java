package net.jan.moddirector.core.util;

import java.io.IOException;
import java.io.InputStream;

public class WebGetResponse implements AutoCloseable {
    private final InputStream inputStream;
    private final long streamSize;

    public WebGetResponse(InputStream inputStream, long streamSize) {
        this.inputStream = inputStream;
        this.streamSize = streamSize;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public long getStreamSize() {
        return streamSize;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
