package net.jan.moddirector.standalone;

import net.jan.moddirector.core.ModDirector;

import java.util.concurrent.TimeUnit;

public class ModDirectorStandalone {
    public static void main(String[] args) throws InterruptedException {
        ModDirectorStandalonePlatform platform = new ModDirectorStandalonePlatform();
        ModDirector director = ModDirector.bootstrap(platform);

        if(!director.activate(Long.MAX_VALUE, TimeUnit.DAYS)) {
            director.errorExit();
        }

        System.out.println("============================================================");
        System.out.println("Installed mods summary:");
        System.out.println("============================================================");
        director.getInstalledMods().forEach((mod) -> {
            System.out.println(mod.getFile() + (mod.shouldInject() ? " has been injected" : " has not been injected"));
            mod.getOptions().forEach((key, value) -> System.out.println("- " + key + ": " + value));
        });
        System.out.println("============================================================");
    }
}
