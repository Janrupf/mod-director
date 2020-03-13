package net.jan.moddirector.standalone;

import net.jan.moddirector.core.ModDirectorPlatform;
import net.jan.moddirector.core.logging.ModDirectorLogger;

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
    public void bootstrap() {
    }

    @Override
    public boolean headless() {
        return false;
    }
}
