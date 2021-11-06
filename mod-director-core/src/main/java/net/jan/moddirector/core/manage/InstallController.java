package net.jan.moddirector.core.manage;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.configuration.RemoteModInformation;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.manage.install.InstallableMod;
import net.jan.moddirector.core.manage.install.InstalledMod;
import net.jan.moddirector.core.util.HashResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

public class InstallController {
    private final ModDirector director;

    public InstallController(ModDirector director) {
        this.director = director;
    }

    private ModDirectorSeverityLevel downloadSeverityLevelFor(ModDirectorRemoteMod mod) {
        return mod.getInstallationPolicy().shouldContinueOnFailedDownload() ?
                ModDirectorSeverityLevel.WARN : ModDirectorSeverityLevel.ERROR;
    }

    public List<Callable<Void>> createPreInstallTasks(
            List<ModDirectorRemoteMod> allMods,
            List<ModDirectorRemoteMod> excludedMods,
            List<InstallableMod> freshMods,
            List<InstallableMod> reinstallMods,
            BiFunction<String, String, ProgressCallback> callbackFactory
    ) {
        List<Callable<Void>> preInstallTasks = new ArrayList<>();

        for(ModDirectorRemoteMod mod : allMods) {
            preInstallTasks.add(() -> {
                ProgressCallback callback = callbackFactory.apply(mod.offlineName(), "Checking installation status");

                callback.indeterminate(true);
                callback.message("Checking installation requirements");

                if(mod.getMetadata() != null && !mod.getMetadata().shouldTryInstall(director)) {
                    director.getLogger().log(
                            ModDirectorSeverityLevel.DEBUG,
                            "ModDirector/InstallSelector",
                            "CORE",
                            "Skipping mod %s because shouldTryInstall() returned false",
                            mod.offlineName()
                    );

                    excludedMods.add(mod);

                    callback.done();
                    return null;
                }

                callback.message("Querying mod information");

                RemoteModInformation information;

                try {
                    information = mod.queryInformation();
                } catch(ModDirectorException e) {
                    director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                            "CORE", e, "Failed to query information for %s from %s",
                            mod.offlineName(), mod.remoteType());
                    director.addError(new ModDirectorError(downloadSeverityLevelFor(mod),
                            "Failed to query information for mod " + mod.offlineName() + " from " + mod.remoteType(),
                            e));
                    callback.done();
                    return null;
                }

                callback.title(information.getDisplayName());
                Path targetFile = computeInstallationTargetPath(mod, information);

                if(targetFile == null) {
                    callback.done();
                    return null;
                }

                Path disabledFile = computeDisabledPath(targetFile);
                InstallableMod installableMod = new InstallableMod(mod, information, targetFile);

                if(Files.isRegularFile(disabledFile)) {
                    excludedMods.add(mod);
                    callback.done();
                    return null;
                }

                if(mod.getMetadata() != null && Files.isRegularFile(targetFile)) {
                    HashResult hashResult = mod.getMetadata().checkHashes(targetFile, director);

                    switch(hashResult) {
                        case UNKNOWN:
                            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController",
                                    "CORE", "Skipping download of %s as hashes can't be determined but file exists",
                                    targetFile.toString());
                            callback.done();

                            excludedMods.add(mod);
                            return null;

                        case MATCHED:
                            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/InstallController",
                                    "CORE", "Skipping download of %s as the hashes match", targetFile.toString());
                            callback.done();

                            excludedMods.add(mod);
                            return null;

                        case UNMATCHED:
                            director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/InstallController",
                                    "CORE", "File %s exists, but hashes do not match, downloading again!",
                                    targetFile.toString());
                    }

                    reinstallMods.add(installableMod);
                } else if(Files.isRegularFile(targetFile)) {
                    director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController",
                            "CORE", "File %s exists and no metadata given, skipping download.",
                            targetFile.toString());
                    excludedMods.add(mod);
                } else {
                    freshMods.add(installableMod);
                }

                callback.done();
                return null;
            });
        }

        return preInstallTasks;
    }

    private Path computeInstallationTargetPath(ModDirectorRemoteMod mod, RemoteModInformation information) {
        Path installationRoot = director.getPlatform().installationRoot().toAbsolutePath().normalize();

        Path targetFile = (mod.getFolder() == null ?
                director.getPlatform().modFile(information.getTargetFilename())
                : mod.getFolder().equalsIgnoreCase(".") ?
                director.getPlatform().rootFile(information.getTargetFilename())
                : director.getPlatform().customFile(information.getTargetFilename(), mod.getFolder()))
                .toAbsolutePath().normalize();

        if(!targetFile.startsWith(installationRoot)) {
            director.getLogger().log(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", "Tried to install a file to %s, which is outside the installation root of %s!",
                    targetFile.toString(), director.getPlatform().installationRoot());
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Tried to install a file to " + targetFile + ", which is outside of " +
                            "the installation root " + installationRoot));
            return null;
        }

        return targetFile;
    }

    private Path computeDisabledPath(Path modFile) {
        return modFile.resolveSibling(modFile.getFileName() + ".disabled-by-mod-director");
    }

    public void markDisabledMods(List<InstallableMod> mods) {
        for(InstallableMod mod : mods) {
            try {
                Path disabledFile = computeDisabledPath(mod.getTargetFile());

                Files.createDirectories(disabledFile.getParent());
                Files.createFile(disabledFile);
            } catch (IOException e) {
                director.getLogger().logThrowable(
                        ModDirectorSeverityLevel.WARN,
                        "ModDirector/InstallController",
                        "CORE",
                        e,
                        "Failed to create disabled file, the user might be asked again if he wants to install the mod"
                );

                director.addError(new ModDirectorError(
                        ModDirectorSeverityLevel.WARN,
                        "Failed to create disabled file",
                        e
                ));
            }
        }
    }

    public List<Callable<Void>> createInstallTasks(
            List<InstallableMod> mods,
            BiFunction<String, String, ProgressCallback> callbackFactory
    ) {
        List<Callable<Void>> installTasks = new ArrayList<>();

        for(InstallableMod mod : mods) {
            installTasks.add(() -> {
                handle(mod, callbackFactory.apply(mod.getRemoteMod().offlineName(), "Installing"));
                return null;
            });
        }

        return installTasks;
    }

    private void handle(InstallableMod mod, ProgressCallback callback) {
        ModDirectorRemoteMod remoteMod = mod.getRemoteMod();

        director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/InstallController", "CORE",
                "Now handling %s from backend %s", remoteMod.offlineName(), remoteMod.remoteType());

        Path targetFile = mod.getTargetFile();

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
            mod.performInstall(director, callback);
        } catch(ModDirectorException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", e, "Failed to install mod %s", remoteMod.offlineName());
            director.addError(new ModDirectorError(downloadSeverityLevelFor(remoteMod),
                    "Failed to install mod "  + remoteMod.offlineName(), e));
            callback.done();
            return;
        }

        if(remoteMod.getMetadata() != null && remoteMod.getMetadata().checkHashes(targetFile, director) == HashResult.UNMATCHED) {
            director.getLogger().log(ModDirectorSeverityLevel.ERROR, "ModDirector/InstallController",
                    "CORE", "Mod did not match hash after download, aborting!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Mod did not match hash after download"));
        } else {
            director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/InstallController",
                    "CORE", "Installed mod file %s", targetFile.toString());
            director.installSuccess(new InstalledMod(targetFile, remoteMod.getOptions(), remoteMod.forceInject()));
        }

        callback.done();
    }
}
