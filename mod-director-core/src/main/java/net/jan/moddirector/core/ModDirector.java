package net.jan.moddirector.core;

import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.logging.ModDirectorLogger;
import net.jan.moddirector.core.configuration.ConfigurationController;
import net.jan.moddirector.core.manage.InstallController;
import net.jan.moddirector.core.manage.InstalledMod;
import net.jan.moddirector.core.manage.ModDirectorError;
import net.jan.moddirector.core.manage.NullProgressCallback;
import net.jan.moddirector.core.ui.InstallProgressDialog;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ModDirector {
    private static ModDirector instance;

    public static ModDirector bootstrap(ModDirectorPlatform platform) {
        if(instance != null) {
            throw new IllegalStateException("ModDirector has already been bootstrapped using platform " +
                    platform.name());
        }

        instance = new ModDirector(platform);
        instance.bootstrap();
        return instance;
    }

    public static ModDirector getInstance() {
        if(instance == null) {
            throw new IllegalStateException("ModDirector has not been bootstrapped yet");
        }

        return instance;
    }

    private final ModDirectorPlatform platform;
    private final ModDirectorLogger logger;
    private final ConfigurationController configurationController;
    private final InstallController installController;
    private final List<ModDirectorError> errors;
    private final List<InstalledMod> installedMods;
    private final ExecutorService executorService;

    private ModDirector(ModDirectorPlatform platform) {
        this.platform = platform;
        this.logger = platform.logger();

        this.configurationController = new ConfigurationController(this, platform.configurationDirectory());
        this.installController = new InstallController(this);

        this.errors = new LinkedList<>();
        this.installedMods = new LinkedList<>();
        this.executorService = Executors.newFixedThreadPool(4);

        logger.log(ModDirectorSeverityLevel.INFO, "ModDirector", "CORE", "Mod director loaded!");
    }

    private void bootstrap() {
        platform.bootstrap();
    }

    public boolean activate(long timeout, TimeUnit timeUnit) throws InterruptedException {
        List<ModDirectorRemoteMod> mods = configurationController.load();
        if(hasFatalError()) {
            return false;
        }

        InstallProgressDialog progressDialog = null;
        if(!platform.headless()) {
            progressDialog = new InstallProgressDialog();
            progressDialog.setLocationRelativeTo(null);
            progressDialog.setVisible(true);
        }

        InstallProgressDialog finalProgressDialog = progressDialog;
        mods.forEach(mod -> executorService.submit(() -> {
            try {
                installController.handle(mod,
                        finalProgressDialog != null ?
                                finalProgressDialog.createProgressCallback(
                                        mod.offlineName(), "Preparing install") :
                                new NullProgressCallback());
            } catch(Exception e) {
                logger.logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector", "CORE", e,
                        "Unhandled exception in worker thread");
                addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                        "Unhandled exception in worker thread", e));
            }
        }));
        executorService.shutdown();
        executorService.awaitTermination(timeout, timeUnit);

        if(progressDialog != null) {
            progressDialog.dispose();
        }

        return !hasFatalError();
    }

    public ModDirectorLogger getLogger() {
        return logger;
    }

    public ModDirectorPlatform getPlatform() {
        return platform;
    }

    public void addError(ModDirectorError error) {
        synchronized(errors) {
            errors.add(error);
        }
    }

    public boolean hasFatalError() {
        return errors.stream().anyMatch(e -> e.getLevel() == ModDirectorSeverityLevel.ERROR);
    }

    public void installSuccess(InstalledMod mod) {
        synchronized(installedMods) {
            installedMods.add(mod);
        }
    }

    public List<InstalledMod> getInstalledMods() {
        return Collections.unmodifiableList(installedMods);
    }

    public void errorExit() {
        logger.log(ModDirectorSeverityLevel.ERROR, "ModDirector", "CORE",
                "============================================================");
        logger.log(ModDirectorSeverityLevel.ERROR, "ModDirector", "CORE",
                "Summary of %d encountered errors:", errors.size());
        errors.forEach(e -> {
            if(e.getException() != null) {
                logger.logThrowable(e.getLevel(), "ModDirector", "CORE", e.getException(), e.getMessage());
            } else {
                logger.log(e.getLevel(), "ModDirector", "CORE", e.getMessage());
            }
        });
        logger.log(ModDirectorSeverityLevel.ERROR, "ModDirector", "CORE",
                "============================================================");
        System.exit(1);
    }
}
