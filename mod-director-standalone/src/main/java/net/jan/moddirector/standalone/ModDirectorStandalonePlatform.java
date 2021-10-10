package net.jan.moddirector.standalone;

import net.jan.moddirector.core.platform.ModDirectorPlatform;
import net.jan.moddirector.core.logging.ModDirectorLogger;
import net.jan.moddirector.core.platform.PlatformSide;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ModDirectorStandalonePlatform implements ModDirectorPlatform {
    private final ModDirectorLogger logger;

    public ModDirectorStandalonePlatform() {
        this.logger = new ModDirectorStandaloneLogger();
    }

    @Override
    public String name() {
        return "Standalone";
    }

    @Override
    public Path configurationDirectory() {
        return Paths.get(".");
    }

    @Override
    public Path modFile(String modFileName) {
        return Paths.get(".", modFileName);
    }

    @Override
    public ModDirectorLogger logger() {
        return logger;
    }

    @Override
    public PlatformSide side() {
        return null;
    }

    @Override
    public void bootstrap() {
    }

    @Override
    public boolean headless() {
        return false;
    }

    @Override
    public Path customFile(String modFileName, String modFolderName) {
        return Paths.get(".", modFolderName).resolve(modFileName);
    }

    @Override
    public Path rootFile(String modFileName) {
        return Paths.get(".", modFileName);
    }
}
