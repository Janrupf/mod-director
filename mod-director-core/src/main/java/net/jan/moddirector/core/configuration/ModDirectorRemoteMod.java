package net.jan.moddirector.core.configuration;

import net.jan.moddirector.core.exception.ModDirectorException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public abstract class ModDirectorRemoteMod {
    private final RemoteModHash hash;
    private final Map<String, Object> options;

    public ModDirectorRemoteMod(RemoteModHash hash, Map<String, Object> options) {
        this.hash = hash;
        this.options = options == null ? Collections.emptyMap() : options;
    }

    public abstract String remoteType();
    public abstract String offlineName();

    public abstract void performInstall(Path targetFile) throws ModDirectorException;
    public abstract RemoteModInformation queryInformation() throws ModDirectorException;

    public RemoteModHash getHash() {
        return hash;
    }

    public Map<String, Object> getOptions() {
        return options;
    }
}
