package net.jan.moddirector.core.manage.check;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Pattern;

public class StopModRepostsEntry {
    private final String domain;
    private final String path;
    private final Pattern pattern;
    private final int advertising;
    private final int redistribution;
    private final int miscellaneous;
    private final String notes;

    @JsonCreator
    public StopModRepostsEntry(
            @JsonProperty(value = "domain", required = true) String domain,
            @JsonProperty(value = "path", required = true) String path,
            @JsonProperty(value = "pattern", required = true) Pattern pattern,
            @JsonProperty(value = "advertising", required = true) int advertising,
            @JsonProperty(value = "redistribution", required = true) int redistribution,
            @JsonProperty(value = "miscellaneous", required = true) int miscellaneous,
            @JsonProperty(value = "notes", required = true) String notes
    ) {
        this.domain = domain;
        this.path = path;
        this.pattern = pattern;
        this.advertising = advertising;
        this.redistribution = redistribution;
        this.miscellaneous = miscellaneous;
        this.notes = notes;
    }

    public String domain() {
        return domain;
    }

    public String path() {
        return path;
    }

    public Pattern pattern() {
        return pattern;
    }

    public int advertising() {
        return advertising;
    }

    public int redistribution() {
        return redistribution;
    }

    public int miscellaneous() {
        return miscellaneous;
    }

    public String notes() {
        return notes;
    }
}
