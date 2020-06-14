package net.jan.moddirector.launchwrapper;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.platform.PlatformSide;

import java.net.URL;

public class SideDetermination {
    private boolean hasBeenDetermined;
    private PlatformSide side;

    public void determine(ModDirector director) {
        director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/SideDetermination", "Launchwrapper",
                "Trying to determine side...");

        URL minecraftMainClass =
                director.getClass().getClassLoader().getResource("net/minecraft/client/main/Main.class");
        if(minecraftMainClass != null) {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/SideDetermination", "Launchwrapper",
                    "Found minecraft client main class at %s, assuming we are running on a client!",
                    minecraftMainClass.toExternalForm());
            side = PlatformSide.CLIENT;
        } else {
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/SideDetermination", "Launchwrapper",
                    "Unable to find minecraft client main class, assuming we are running on a server!");
            side = PlatformSide.SERVER;
        }

        hasBeenDetermined = true;
    }

    public PlatformSide get() {
        if(!hasBeenDetermined) {
            throw new IllegalStateException("Side has not been determined yet");
        }

        return side;
    }
}
