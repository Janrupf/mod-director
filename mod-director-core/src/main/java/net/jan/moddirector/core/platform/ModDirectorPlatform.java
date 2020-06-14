package net.jan.moddirector.core.platform;

import net.jan.moddirector.core.logging.ModDirectorLogger;

import java.nio.file.Path;

public interface ModDirectorPlatform {
    String name();
    Path configurationDirectory();
    Path modFile(String modFileName);
    ModDirectorLogger logger();
    PlatformSide side();

    void bootstrap();
    boolean headless();
}
