package net.jan.moddirector.core;

import net.jan.moddirector.core.logging.ModDirectorLogger;

import java.nio.file.Path;

public interface ModDirectorPlatform {
    String name();
    Path configurationDirectory();
    Path modFile(String modFileName);
    ModDirectorLogger logger();

    void bootstrap();
    boolean headless();
}
