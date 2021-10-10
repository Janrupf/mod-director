package net.jan.moddirector.launchwrapper;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.platform.ModDirectorPlatform;
import net.jan.moddirector.core.logging.ModDirectorLogger;
import net.jan.moddirector.core.platform.PlatformSide;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class LaunchwrapperModDirectorPlatform implements ModDirectorPlatform {
    private final ModDirectorTweaker tweaker;
    private final SideDetermination sideDetermination;

    public LaunchwrapperModDirectorPlatform(ModDirectorTweaker tweaker) {
        this.tweaker = tweaker;
        this.sideDetermination = new SideDetermination();
    }

    @Override
    public String name() {
        return "Launchwrapper";
    }

    @Override
    public Path configurationDirectory() {
        File configDir = new File(tweaker.getGameDir(), "config/mod-director");
        if(!configDir.exists() && !configDir.mkdirs()) {
            throw new UncheckedIOException(new IOException("Failed to create config directory " +
                    configDir.getAbsolutePath()));
        }

        return configDir.toPath();
    }

    @Override
    public Path modFile(String modFileName) {
        return tweaker.getGameDir().toPath().resolve("mods").resolve(modFileName);
    }
    
    @Override
    public Path customFile(String modFileName, String modFolderName) {
        return tweaker.getGameDir().toPath().resolve(modFolderName).resolve(modFileName);
    }
    
    @Override
    public Path rootFile(String modFileName) {
        return tweaker.getGameDir().toPath().resolve(modFileName);
    }

    @Override
    public ModDirectorLogger logger() {
        return new LaunchwrapperModDirectorLogger();
    }

    @Override
    public PlatformSide side() {
        return sideDetermination.get();
    }

    @Override
    public void bootstrap() {
        sideDetermination.determine(ModDirector.getInstance());
    }

    @Override
    public boolean headless() {
        return GraphicsEnvironment.isHeadless();
    }
}
