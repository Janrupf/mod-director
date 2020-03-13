package net.jan.moddirector.launchwrapper;

import net.jan.moddirector.core.ModDirectorPlatform;
import net.jan.moddirector.core.logging.ModDirectorLogger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class LaunchwrapperModDirectorPlatform implements ModDirectorPlatform {
    private final ModDirectorTweaker tweaker;

    public LaunchwrapperModDirectorPlatform(ModDirectorTweaker tweaker) {
        this.tweaker = tweaker;
    }

    @Override
    public String name() {
        return "Launchwrapper";
    }

    @Override
    public Path configurationDirectory() {
        File configDir = new File(tweaker.getGameDir(), "config/mod-director");
        if(!configDir.exists() && !configDir.mkdirs()) {
            throw new UncheckedIOException(new IOException("Failed to create config diretory " +
                    configDir.getAbsolutePath()));
        }

        return configDir.toPath();
    }

    @Override
    public Path modFile(String modFileName) {
        return tweaker.getGameDir().toPath().resolve("mods").resolve(modFileName);
    }

    @Override
    public ModDirectorLogger logger() {
        return new LaunchwrapperModDirectorLogger();
    }

    @Override
    public void bootstrap() {

    }

    @Override
    public boolean headless() {
        return GraphicsEnvironment.isHeadless();
    }
}
