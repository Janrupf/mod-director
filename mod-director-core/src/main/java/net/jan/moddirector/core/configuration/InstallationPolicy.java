package net.jan.moddirector.core.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InstallationPolicy {
    private final boolean continueOnFailedDownload;
    private final String optionalKey;
    private final boolean selectedByDefault;
    private final String name;
    private final String description;

    @JsonCreator
    public InstallationPolicy(
            @JsonProperty(value = "continueOnFailedDownload") boolean continueOnFailedDownload,
            @JsonProperty(value = "optionalKey") String optionalKey,
            @JsonProperty(value = "selectedByDefault") Boolean selectedByDefault,
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "description") String description
    ) {
        this.continueOnFailedDownload = continueOnFailedDownload;
        this.optionalKey = optionalKey;
        this.selectedByDefault = selectedByDefault != null ? selectedByDefault : optionalKey != null;
        this.name = name;
        this.description = description;
    }

    public boolean shouldContinueOnFailedDownload() {
        return continueOnFailedDownload;
    }

    public String getOptionalKey() {
        return optionalKey;
    }

    public boolean isSelectedByDefault() {
        return selectedByDefault;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
