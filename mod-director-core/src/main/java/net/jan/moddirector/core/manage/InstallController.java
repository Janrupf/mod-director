package net.jan.moddirector.core.manage;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.configuration.RemoteModInformation;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.util.HashResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InstallController {
    private final ModDirector director;

    public InstallController(ModDirector director) {
        this.director = director;
    }

    public void handle(ModDirectorRemoteMod mod, ProgressCallback callback) {
        director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController", "CORE",
                "Now handling %s from backend %s", mod.offlineName(), mod.remoteType());

        if(mod.getMetadata() != null && !mod.getMetadata().shouldTryInstall(director)) {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController", "CORE",
                    "Skipping install, shouldTryInstall() returned false");
            return;
        }

        callback.indeterminate(true);
        callback.message("Querying mod information");
        RemoteModInformation information;

        try {
            information = mod.queryInformation();
        } catch(ModDirectorException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", e, "Failed to query information for %s from %s",
                    mod.offlineName(), mod.remoteType());
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to query information for mod " + mod.offlineName() + " from " + mod.remoteType(),
                    e));
            return;
        }

        callback.title(information.getDisplayName());

        Path targetFile = mod.folderName() == null ? director.getPlatform().modFile(information.getTargetFilename()) 
            : mod.folderName().equalsIgnoreCase(".") ? director.getPlatform().rootFile(information.getTargetFilename()) 
                : director.getPlatform().customFile(information.getTargetFilename(), mod.folderName());

        if(mod.getMetadata() != null && Files.isRegularFile(targetFile)) {
            HashResult hashResult = mod.getMetadata().checkHashes(targetFile, director);

            switch(hashResult) {
                case UNKNOWN:
                    director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController",
                            "CORE", "Skipping download of %s as hashes can't be determined but file exists",
                            targetFile.toString());
                    callback.done();
                    return;

                case MATCHED:
                    director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/InstallController",
                            "CORE", "Skipping download of %s as the hashes match", targetFile.toString());
                    callback.done();
                    return;

                case UNMATCHED:
                    director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/InstallController",
                            "CORE", "File %s exists, but hashes do not match, downloading again!",
                            targetFile.toString());
            }
        } else if(Files.isRegularFile(targetFile)) {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController",
                    "CORE", "File %s exists and no metadata given, skipping download.",
                    targetFile.toString());
            callback.done();
            return;
        }

        try {
            Files.createDirectories(targetFile.getParent());
        } catch(IOException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", e, "Failed to create directory %s", targetFile.getParent().toString());
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to create directory" + targetFile.getParent().toString(), e));
            callback.done();
            return;
        }

        try {
            mod.performInstall(targetFile, callback, director, information);
        } catch(ModDirectorException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", e, "Failed to install mod %s", mod.offlineName());
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to install mod " + mod.offlineName()));
            callback.done();
            return;
        }

        if(mod.getMetadata() != null && mod.getMetadata().checkHashes(targetFile, director) == HashResult.UNMATCHED) {
            director.getLogger().log(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", "Mod did not match hash after download, aborting!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Mod did not match hash after download"));
        } else {
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/InstallController",
                    "CORE", "Installed mod file %s", targetFile.toString());
            director.installSuccess(new InstalledMod(targetFile, mod.getOptions(), mod.forceInject()));
        }

        callback.done();
    }
}
