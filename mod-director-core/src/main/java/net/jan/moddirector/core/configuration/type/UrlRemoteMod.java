package net.jan.moddirector.core.configuration.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.configuration.RemoteModHash;
import net.jan.moddirector.core.configuration.RemoteModInformation;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.util.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlRemoteMod extends ModDirectorRemoteMod {
    private final String fileName;
    private final URL url;
    private final String[] follows;

    @JsonCreator
    public UrlRemoteMod(
            @JsonProperty(value = "fileName") String fileName,
            @JsonProperty(value = "url", required = true) URL url,
            @JsonProperty(value = "follows") String[] follows,
            @JsonProperty(value = "hash") RemoteModHash hash,
            @JsonProperty(value = "options") Map<String, Object> options
    ) {
        super(hash, options);
        this.fileName = fileName;
        this.url = url;
        this.follows = follows == null ? new String[0] : follows;
    }

    @Override
    public String remoteType() {
        return url.toExternalForm();
    }

    @Override
    public String offlineName() {
        return url.getFile().isEmpty() ? "<no name>" : url.getFile();
    }

    @Override
    public void performInstall(Path targetFile) throws ModDirectorException {
        byte[] data = null;

        URL urlToFollow = null;
        for(int i = -1; i < follows.length; i++) {
            if(i < 0) {
                urlToFollow = url;
            } else {
                String html = new String(data);

                int startIndex = html.indexOf(follows[i]);
                if(startIndex < 0) {
                    throw new ModDirectorException("Unable to find follow string " + follows[i] + " in html from " +
                            urlToFollow.toString());
                }

                int href = html.substring(0, startIndex).lastIndexOf("href=") + 5;
                char hrefEnclose = html.charAt(href);
                int hrefEnd = html.indexOf(hrefEnclose, href + 2);

                String newUrl = html.substring(href + 1, hrefEnd);
                if(newUrl.isEmpty()) {
                    throw new ModDirectorException("Result url was empty when matching " + follows[i] +
                            " in html from " + urlToFollow.toString());
                }

                try {
                    if(!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                        if(!newUrl.startsWith("/")) {
                            newUrl = "/" + newUrl;
                        }
                        urlToFollow = new URL(urlToFollow.getProtocol(), urlToFollow.getHost(), newUrl);
                    } else {
                        urlToFollow = new URL(newUrl);
                    }
                } catch(MalformedURLException e) {
                    throw new ModDirectorException("Failed to create follow url when using follow " + follows[i], e);
                }
            }

            try {
                InputStream websiteStream = WebClient.get(urlToFollow);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[8192];
                int len;
                while((len = websiteStream.read(buffer)) > -1) {
                    outputStream.write(buffer, 0, len);
                }

                data = outputStream.toByteArray();
                outputStream.close();
                websiteStream.close();
            } catch(IOException e) {
                throw new ModDirectorException("Failed to follow URL's to download file", e);
            }
        }

        try {
            Files.write(targetFile, data);
        } catch(IOException e) {
            throw new ModDirectorException("Failed to write file to disk", e);
        }
    }

    @Override
    public RemoteModInformation queryInformation() {
        if(fileName != null) {
            return new RemoteModInformation(fileName);
        } else {
            return new RemoteModInformation(Paths.get(url.getFile()).getFileName().toString());
        }
    }
}
