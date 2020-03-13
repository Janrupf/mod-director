package net.jan.moddirector.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WebClient {
    public static final String USER_AGENT = "ModDirector v=0.1.0";

    public static InputStream get(URL url) throws IOException  {
        URLConnection connection = url.openConnection();
        if(!(connection instanceof HttpURLConnection)) {
            return connection.getInputStream();
        }

        int redirectCount = 0;
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpConnection.connect();

        while(true) {
            int status = httpConnection.getResponseCode();
            if(status - 300 >= 0 && status - 300 <= 99) {
                if(redirectCount > 10) {
                    throw new IOException("Server tried to redirect too many times");
                }

                String newUrl = httpConnection.getHeaderField("Location");
                String cookies = httpConnection.getHeaderField("Set-Cookie");

                httpConnection.getInputStream().close();
                httpConnection.disconnect();

                try {
                    url = new URL(newUrl);
                    connection = url.openConnection();

                    if(!(connection instanceof HttpURLConnection)) {
                        throw new IOException("Server sent a redirect url which was not http: " + newUrl);
                    }

                    redirectCount++;

                    httpConnection = (HttpURLConnection) connection;
                    httpConnection.setRequestProperty("Cookie", cookies);
                    httpConnection.setRequestProperty("User-Agent", USER_AGENT);
                    httpConnection.connect();
                } catch(MalformedURLException e) {
                    throw new IOException("Server sent invalid redirect url", e);
                }
            } else {
                break;
            }
        }

        return httpConnection.getInputStream();
    }
}
