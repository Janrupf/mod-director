package net.jan.moddirector.core.manage.check;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Pattern;

public class StopModRepostsEntry {
    private final String domain;
    private final String path;
    private final String reason;
    private final String notes;

    @JsonCreator
    public StopModRepostsEntry(
            @JsonProperty(value = "domain", required = true) String domain,
            @JsonProperty(value = "path", required = true) String path,
            @JsonProperty(value = "reason", required = true) String reason,
            @JsonProperty(value = "notes", required = true) String notes
    ) {
        this.domain = domain;
        this.path = path;
        this.reason = reason;
        this.notes = notes;
    }

    public String domain() {
        return domain;
    }

    public String path() {
        return path;
    }
    
    public int reason() {
        return reason;
    }

    public String notes() {
        return notes;
    }
}
