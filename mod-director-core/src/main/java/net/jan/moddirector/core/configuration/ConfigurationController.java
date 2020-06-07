package net.jan.moddirector.core.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.configuration.modpack.ModpackConfiguration;
import net.jan.moddirector.core.configuration.type.CurseRemoteMod;
import net.jan.moddirector.core.configuration.type.UrlRemoteMod;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.manage.ModDirectorError;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfigurationController {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper instance = new ObjectMapper();
        instance.setDefaultLeniency(false);
        instance.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        return instance;
    }

    private final ModDirector director;
    private final Path configurationDirectory;
    private final List<ModDirectorRemoteMod> configurations;

    private ModpackConfiguration modpackConfiguration;

    public ConfigurationController(ModDirector director, Path configurationDirectory) {
        this.director = director;
        this.configurationDirectory = configurationDirectory;
        this.configurations = new ArrayList<>();
    }

    public void load() {
        Path modpackConfigPath = configurationDirectory.resolve("modpack.json");
        if(Files.exists(modpackConfigPath)) {
            if(!loadModpackConfiguration(modpackConfigPath)) {
                return;
            }
        }

        try(Stream<Path> paths = Files.walk(configurationDirectory)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .filter(p -> !p.getFileName().toString().equals("modpack.json"))
                    .forEach(this::addConfig);
        } catch(IOException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ConfigurationController",
                    "CORE", e, "Failed to iterate configuration directory!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to iterate configuration directory", e));
        }
    }

    private boolean loadModpackConfiguration(Path configurationPath) {
        try(InputStream stream = Files.newInputStream(configurationPath)) {
            modpackConfiguration = OBJECT_MAPPER.readValue(stream, ModpackConfiguration.class);
            return true;
        } catch (IOException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ConfigurationController",
                    "CORE", e, "Failed to read modpack configuration!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to read modpack configuration!"));
            return false;
        }
    }

    private void addConfig(Path configurationPath) {
        director.getLogger().log(ModDirectorSeverityLevel.INFO, "ModDirector/ConfigurationController",
                "CORE", "Loading config %s", configurationPath.toString());

        Class<? extends ModDirectorRemoteMod> targetType = getTypeForFile(configurationPath);
        if(targetType == null) {
            return;
        }

        try(InputStream stream = Files.newInputStream(configurationPath)) {
            configurations.add(OBJECT_MAPPER.readValue(stream, targetType));
        } catch(JsonParseException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ConfigurationController",
                    "CORE", e, "Failed to parse a configuration!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to parse a configuration", e));
        } catch(IOException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.ERROR, "ModDirector/ConfigurationController",
                    "CORE", e, "Failed to open a configuration for reading!");
            director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                    "Failed to open a configuration for reading", e));
        }
    }

    private Class<? extends ModDirectorRemoteMod> getTypeForFile(Path file) {
        String name = file.toString();
        if(name.endsWith(".curse.json")) {
            return CurseRemoteMod.class;
        } else if(name.endsWith(".url.json")) {
            return UrlRemoteMod.class;
        } else {
            director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/ConfigurationController",
                    "CORE", "Ignoring unknown json file %s", name);
            return null;
        }
    }

    public ModpackConfiguration getModpackConfiguration() {
        return modpackConfiguration;
    }

    public List<ModDirectorRemoteMod> getConfigurations() {
        return configurations;
    }
}
