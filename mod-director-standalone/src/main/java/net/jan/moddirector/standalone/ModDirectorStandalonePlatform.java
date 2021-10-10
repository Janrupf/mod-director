package net.jan.moddirector.standalone;

import net.jan.moddirector.core.platform.ModDirectorPlatform;
import net.jan.moddirector.core.logging.ModDirectorLogger;
import net.jan.moddirector.core.platform.PlatformSide;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
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
        return getPathAndCreate(".", "config", "mod-director");
    }

    @Override
    public Path modFile(String modFileName) {
        return getPathAndCreate(".", "mods").resolve(modFileName);
    }

    @Override
    public Path rootFile(String modFileName) {
        return getPathAndCreate(".").resolve(modFileName);
    }

    @Override
    public Path customFile(String modFileName, String modFolderName) {
        return getPathAndCreate(".", modFolderName).resolve(modFileName);
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

    private Path getPathAndCreate(String first, String... more) {
        Path p = Paths.get(first, more);

        if(!Files.isDirectory(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return p;
    }
}
