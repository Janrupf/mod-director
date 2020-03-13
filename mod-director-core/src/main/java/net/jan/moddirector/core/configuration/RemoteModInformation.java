package net.jan.moddirector.core.configuration;

public class RemoteModInformation {
    private final String targetFilename;

    public RemoteModInformation(String targetFilename) {
        this.targetFilename = targetFilename;
    }

    public String getTargetFilename() {
        return targetFilename;
    }
}
