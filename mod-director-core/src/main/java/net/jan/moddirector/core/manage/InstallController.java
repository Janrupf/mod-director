package net.jan.moddirector.core.manage;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.configuration.RemoteModInformation;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;

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

        Path targetFile = director.getPlatform().modFile(information.getTargetFilename());

        if(mod.getHash() != null && Files.isRegularFile(targetFile)) {
            if(mod.getHash().matches(targetFile, director)) {
                director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController",
                        "CORE", "Skipping download of %s, hashes match.", targetFile.toString());
                callback.done();
                return;
            } else {
                director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/InstallController",
                        "CORE", "File %s exists but hash does not match, downloading again!",
                        targetFile.toString());
            }
        } else if(Files.isRegularFile(targetFile)) {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController",
                    "CORE", "File %s exists and no hash given, skipping download.", targetFile.toString());
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
            mod.performInstall(targetFile, callback);
        } catch(ModDirectorException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", e, "Failed to install mod %s", mod.offlineName());
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to install mod " + mod.offlineName()));
            callback.done();
            return;
        }

        if(mod.getHash() != null && !mod.getHash().matches(targetFile, director)) {
            director.getLogger().log(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", "Mod did not match hash after download, aborting!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Mod did not match hash after download"));
        } else {
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/InstallController",
                    "CORE", "Installed mod file %s", targetFile.toString());
            director.installSuccess(new InstalledMod(targetFile, mod.getOptions()));
        }

        callback.done();
    }
}
