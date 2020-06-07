package net.jan.moddirector.core.ui;

import net.jan.moddirector.core.util.WebClient;
import net.jan.moddirector.core.util.WebGetResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ImageLoader {
    private static final String FILE_PROTOCOL = "file://";
    private static final List<String> WEB_PROTOCOLS = Arrays.asList("https://", "http://");

    public static JLabel createLabelForImage(String path, int width, int height) {
        if (path.startsWith(FILE_PROTOCOL)) {
            return readFromFile(path.substring(7), width, height);
        }

        for (String protocol : WEB_PROTOCOLS) {
            if (path.startsWith(protocol)) {
                return readFromWeb(path, width, height);
            }
        }

        return readFromFile(path, width, height);
    }

    private static JLabel readFromFile(String path, int width, int height) {
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            return errorLabel("File %s not found", path);
        } else if(!imageFile.isFile()) {
            return errorLabel("%s it not a file", path);
        }

        try {
            Image image = ImageIO.read(imageFile).getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(image));
        } catch (IOException e) {
            return errorLabel("Failed to read file %s due to IOException: %s", path, e.getMessage());
        }
    }

    private static JLabel readFromWeb(String path, int width, int height) {
        try(WebGetResponse response = WebClient.get(new URL(path))) {
            Image image = ImageIO.read(response.getInputStream()).getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(image));
        } catch (MalformedURLException e) {
            return errorLabel("%s is not a valid url: %s", path, e.getMessage());
        } catch (IOException e) {
            return errorLabel("Failed to read data from %s due to IOException: %s", path, e.getMessage());
        }
    }

    private static JLabel errorLabel(String fmt, Object... args) {
        JLabel label = new JLabel(String.format(fmt, args));
        label.setForeground(Color.RED);
        return label;
    }
}
