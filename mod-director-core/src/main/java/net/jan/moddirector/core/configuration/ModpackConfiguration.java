package net.jan.moddirector.core.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModpackConfiguration {
    private final String packName;
    private final String icon;

    @JsonCreator
    public ModpackConfiguration(
            @JsonProperty(value = "packName", required = true) String packName,
            @JsonProperty("icon") String icon
    ) {
        this.packName = packName;
        this.icon = icon;
    }

    public String packName() {
        return packName;
    }

    public String icon() {
        return icon;
    }

    public static ModpackConfiguration createDefault() {
        return new ModpackConfiguration(
                "Modpack",
                null
        );
    }
}
