package net.jan.moddirector.core.configuration.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import net.jan.moddirector.core.configuration.ConfigurationController;
import net.jan.moddirector.core.configuration.ModDirectorRemoteMod;
import net.jan.moddirector.core.configuration.RemoteModHash;
import net.jan.moddirector.core.configuration.RemoteModInformation;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.util.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class CurseRemoteMod extends ModDirectorRemoteMod {
    private final int addonId;
    private final int fileId;

    private CurseAddonFileInformation information;

    @JsonCreator
    public CurseRemoteMod(
            @JsonProperty(value = "addonId", required = true) int addonId,
            @JsonProperty(value = "fileId", required = true) int fileId,
            @JsonProperty(value = "hash") RemoteModHash hash,
            @JsonProperty(value = "options") Map<String, Object> options
            ) {
        super(hash, options);
        this.addonId = addonId;
        this.fileId = fileId;
    }

    @Override
    public String remoteType() {
        return "Curse";
    }

    @Override
    public String offlineName() {
        return addonId + ":" + fileId;
    }

    @Override
    public void performInstall(Path targetFile) throws ModDirectorException {
        try(InputStream stream = WebClient.get(information.downloadUrl)) {
            Files.copy(stream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e) {
            throw new ModDirectorException("Failed to download file", e);
        }
    }

    @Override
    public RemoteModInformation queryInformation() throws ModDirectorException {
        try {
            URL apiUrl = new URL(
                    String.format("https://addons-ecs.forgesvc.net/api/v2/addon/%d/file/%d", addonId, fileId));

            information = ConfigurationController.OBJECT_MAPPER.readValue(apiUrl, CurseAddonFileInformation.class);
        } catch(MalformedURLException e) {
            throw new ModDirectorException("Failed to create ForgeSVC api url", e);
        } catch(JsonParseException e) {
            throw new ModDirectorException("Failed to parse Json response from curse", e);
        } catch(JsonMappingException e) {
            throw new ModDirectorException("Failed to map Json response from curse, did they change their api?", e);
        } catch(IOException e) {
            throw new ModDirectorException("Failed to open connection to curse", e);
        }

        return new RemoteModInformation(information.fileName);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CurseAddonFileInformation {
        @JsonProperty
        private String fileName;

        @JsonProperty
        private URL downloadUrl;

        @JsonProperty
        private String[] gameVersion;
    }
}
