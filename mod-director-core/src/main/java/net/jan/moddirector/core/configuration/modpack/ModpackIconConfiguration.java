package net.jan.moddirector.core.configuration.modpack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModpackIconConfiguration {
    private final String path;
    private final int width;
    private final int height;

    @JsonCreator
    public ModpackIconConfiguration(
            @JsonProperty(value = "path", required = true) String path,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height
    ) {
        this.path = path;
        this.width = width;
        this.height = height;
    }

    public String path() {
        return path;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
