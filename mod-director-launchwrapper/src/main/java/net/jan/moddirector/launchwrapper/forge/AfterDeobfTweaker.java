package net.jan.moddirector.launchwrapper.forge;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AfterDeobfTweaker implements ITweaker {
    private final List<ITweaker> lateTweakers;

    @SuppressWarnings("unchecked")
    public AfterDeobfTweaker() {
        lateTweakers = (List<ITweaker>) Launch.blackboard.get("LateTweakers");
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        lateTweakers.forEach(t -> t.acceptOptions(args, gameDir, assetsDir, profile));
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        lateTweakers.forEach(t -> t.injectIntoClassLoader(classLoader));
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        List<String> launchArgs = new ArrayList<>();
        lateTweakers.stream().map(ITweaker::getLaunchArguments).map(Arrays::asList).forEach(launchArgs::addAll);

        return launchArgs.toArray(new String[0]);
    }
}
