package net.jan.moddirector.standalone;

import net.jan.moddirector.core.ModDirector;

import java.util.concurrent.TimeUnit;

public class ModDirectorStandalone {
    public static void main(String[] args) throws InterruptedException {
        ModDirectorStandalonePlatform platform = new ModDirectorStandalonePlatform();
        ModDirector director = ModDirector.bootstrap(platform);

        director.activate(-1, TimeUnit.MILLISECONDS);
    }
}
