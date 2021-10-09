package net.jan.moddirector.core.configuration;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.manage.ProgressCallback;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ModDirectorRemoteMod {
    private final RemoteModMetadata metadata;
    private final Map<String, Object> options;
    private final String folderName;
    private Boolean inject;

    public ModDirectorRemoteMod(RemoteModMetadata metadata, Map<String, Object> options, String folderName, Boolean inject) {
        this.metadata = metadata;
        this.options = options == null ? Collections.emptyMap() : options;
        this.folderName = folderName;
        if(inject == null) {
            this.inject = folderName == null;
        } else {
            this.inject = inject;
        }
    }

    public abstract String remoteType();
    public abstract String offlineName();

    public abstract RemoteModInformation queryInformation() throws ModDirectorException;
    public abstract void performInstall(Path targetFile, ProgressCallback progressCallback, ModDirector director,
            RemoteModInformation information) throws ModDirectorException;

    public RemoteModMetadata getMetadata() {
        return metadata;
    }

    public Map<String, Object> getOptions() {
        return options;
    }
    
    public boolean forceInject() {
	    return inject;
    }
	
    public String folderName() {
        return folderName;
    }
}
