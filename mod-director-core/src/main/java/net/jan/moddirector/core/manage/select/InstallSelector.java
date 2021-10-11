package net.jan.moddirector.core.manage.select;

import net.jan.moddirector.core.configuration.InstallationPolicy;
import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.manage.install.InstallableMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstallSelector {
    private final List<InstallableMod> alwaysInstall;
    private final List<SelectableInstallOption> singleOptions;
    private final Map<String, List<SelectableInstallOption>> groupOptions;

    private final Map<SelectableInstallOption, InstallableMod> optionsToMod;

    public InstallSelector() {
        this.alwaysInstall = new ArrayList<>();
        this.singleOptions = new ArrayList<>();
        this.groupOptions = new HashMap<>();
        this.optionsToMod = new HashMap<>();
    }

    public void accept(
            List<ModDirectorRemoteMod> excludedMods,
            List<InstallableMod> freshInstalls,
            List<InstallableMod> reInstall
    ) {
        List<String> ignoredGroups = new ArrayList<>();

        for(ModDirectorRemoteMod mod : excludedMods) {
            String group = mod.getInstallationPolicy().getOptionalKey();

            if(group != null && !group.equals("$")) {
                ignoredGroups.add(group);
            }
        }

        for(InstallableMod mod : reInstall) {
            String group = mod.getRemoteMod().getInstallationPolicy().getOptionalKey();

            if(group != null && !group.equals("$")) {
                ignoredGroups.add(group);
            }

            alwaysInstall.add(mod);
        }

        for(InstallableMod mod : freshInstalls) {
            ModDirectorRemoteMod remoteMod = mod.getRemoteMod();

            if(remoteMod.getInstallationPolicy().getOptionalKey() == null) {
                alwaysInstall.add(mod);
            } else {
                InstallationPolicy policy = remoteMod.getInstallationPolicy();
                if(ignoredGroups.contains(policy.getOptionalKey())) {
                    continue;
                }

                SelectableInstallOption installOption = new SelectableInstallOption(
                        policy.isSelectedByDefault(),
                        policy.getName() == null ? remoteMod.offlineName() : policy.getName(),
                        policy.getDescription()
                );

                if (policy.getOptionalKey().equals("$")) {
                    singleOptions.add(installOption);
                } else {
                    groupOptions.computeIfAbsent(policy.getOptionalKey(), (k) -> new ArrayList<>()).add(installOption);
                }

                optionsToMod.put(installOption, mod);
            }
        }
    }

    public boolean hasSelectableOptions() {
        return !getSingleOptions().isEmpty() || !getGroupOptions().isEmpty();
    }

    public List<SelectableInstallOption> getSingleOptions() {
        return singleOptions;
    }

    public Map<String, List<SelectableInstallOption>> getGroupOptions() {
        return groupOptions;
    }

    public List<InstallableMod> computeModsToInstall() {
        List<InstallableMod> mods = new ArrayList<>(alwaysInstall);

        optionsToMod.forEach((option, mod) -> {
            if(option.isSelected()) {
                mods.add(mod);
            }
        });

        return mods;
    }

    public List<InstallableMod> computeDisabledMods() {
        List<InstallableMod> mods = new ArrayList<>();

        optionsToMod.forEach((option, mod) -> {
            if(!option.isSelected()) {
                mods.add(mod);
            }
        });

        return mods;
    }
}
