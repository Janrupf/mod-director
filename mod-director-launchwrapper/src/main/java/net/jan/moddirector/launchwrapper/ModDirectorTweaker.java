package net.jan.moddirector.launchwrapper;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.launchwrapper.forge.ForgeLateLoader;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModDirectorTweaker implements ITweaker {
    private final ModDirector director;

    private List<String> args;
    private File gameDir;
    private File assetsDir;
    private String profile;
    private LaunchClassLoader classLoader;

    public ModDirectorTweaker() {
        director = ModDirector.bootstrap(new LaunchwrapperModDirectorPlatform(this));
        director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/Tweaker", "Launchwrapper",
                "ModDirector bootstrapped from Launchwrapper!");
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.args = args;
        this.gameDir = gameDir;
        if(gameDir == null) {
            this.gameDir = new File(".").getAbsoluteFile();
            director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "ModDirector/Tweaker", "Launchwrapper",
                    "Fixing null game directory to %s", this.gameDir.getPath());
        }

        this.assetsDir = assetsDir;
        this.profile = profile;
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        this.classLoader = classLoader;

        try {
            if(!director.activate(Long.MAX_VALUE, TimeUnit.DAYS)) {
                director.errorExit();
            }
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

        ForgeLateLoader loader = new ForgeLateLoader(this, director, classLoader);
        loader.execute();
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    public File getGameDir() {
        return gameDir;
    }

    public String getProfile() {
        return profile;
    }

    public void callInjectedTweaker(ITweaker tweaker) {
        tweaker.acceptOptions(args, gameDir, assetsDir, profile);
        tweaker.injectIntoClassLoader(classLoader);
    }
}
