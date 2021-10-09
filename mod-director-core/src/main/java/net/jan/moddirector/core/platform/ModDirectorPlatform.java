package net.jan.moddirector.core.platform;

import net.jan.moddirector.core.logging.ModDirectorLogger;

import java.nio.file.Path;

public interface ModDirectorPlatform {
    String name();
    Path configurationDirectory();
    Path modFile(String modFileName);
    Path rootFile(String modFileName);
    Path customFile(String modFileName, String modFolderName);
    ModDirectorLogger logger();
    PlatformSide side();

    void bootstrap();
    boolean headless();
}
