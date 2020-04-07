package net.jan.moddirector.core.configuration;

import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.manage.ProgressCallback;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public abstract class ModDirectorRemoteMod {
    private final RemoteModMetadata metadata;
    private final Map<String, Object> options;

    public ModDirectorRemoteMod(RemoteModMetadata metadata, Map<String, Object> options) {
        this.metadata = metadata;
        this.options = options == null ? Collections.emptyMap() : options;
    }

    public abstract String remoteType();
    public abstract String offlineName();

    public abstract void performInstall(Path targetFile, ProgressCallback progressCallback) throws ModDirectorException;
    public abstract RemoteModInformation queryInformation() throws ModDirectorException;

    public RemoteModMetadata getMetadata() {
        return metadata;
    }

    public Map<String, Object> getOptions() {
        return options;
    }
}
