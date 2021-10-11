package net.jan.moddirector.core.manage.install;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.configuration.RemoteModInformation;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.manage.ProgressCallback;

import java.nio.file.Path;

public class InstallableMod {
    private final ModDirectorRemoteMod remoteMod;
    private final RemoteModInformation remoteInformation;
    private final Path targetFile;


    public InstallableMod(ModDirectorRemoteMod remoteMod, RemoteModInformation remoteInformation, Path targetFile) {
        this.remoteMod = remoteMod;
        this.remoteInformation = remoteInformation;
        this.targetFile = targetFile;
    }

    public ModDirectorRemoteMod getRemoteMod() {
        return remoteMod;
    }

    public RemoteModInformation getRemoteInformation() {
        return remoteInformation;
    }

    public Path getTargetFile() {
        return targetFile;
    }

    public void performInstall(ModDirector director, ProgressCallback callback) throws ModDirectorException {
        remoteMod.performInstall(targetFile, callback, director, remoteInformation);
    }
}
