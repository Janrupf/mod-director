package net.jan.moddirector.core.configuration;

public class RemoteModInformation {
    private final String displayName;
    private final String targetFilename;

    public RemoteModInformation(String displayName, String targetFilename) {
        this.displayName = displayName;
        this.targetFilename = targetFilename;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTargetFilename() {
        return targetFilename;
    }
}
