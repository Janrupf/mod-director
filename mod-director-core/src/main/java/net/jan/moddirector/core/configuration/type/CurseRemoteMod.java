package net.jan.moddirector.core.configuration.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.configuration.*;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.manage.ProgressCallback;
import net.jan.moddirector.core.util.IOOperation;
import net.jan.moddirector.core.util.WebClient;
import net.jan.moddirector.core.util.WebGetResponse;

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
    private final String folderName;
    private String inject;

    private CurseAddonFileInformation information;

    @JsonCreator
    public CurseRemoteMod(
            @JsonProperty(value = "addonId", required = true) int addonId,
            @JsonProperty(value = "fileId", required = true) int fileId,
            @JsonProperty(value = "metadata") RemoteModMetadata metadata,
            @JsonProperty(value = "options") Map<String, Object> options,
            @JsonProperty(value = "folder") String folderName,
            @JsonProperty(value = "inject") String inject
            ) {
        super(metadata, options);
        this.addonId = addonId;
        this.fileId = fileId;
        this.folderName = folderName;
        this.inject = inject;
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
    public String performInstall(Path targetFile, ProgressCallback progressCallback, ModDirector director, RemoteModInformation information) throws ModDirectorException {
		if (this.folderName != null) {
        	targetFile = director.getPlatform().customFile(information.getTargetFilename(), this.folderName);
        }
		
		try(WebGetResponse response = WebClient.get(this.information.downloadUrl)) {
            progressCallback.setSteps(1);
            IOOperation.copy(response.getInputStream(), Files.newOutputStream(targetFile), progressCallback,
                    response.getStreamSize());
        } catch(IOException e) {
            throw new ModDirectorException("Failed to download file", e);
        }
		
		return this.folderName;
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

        return new RemoteModInformation(information.name, information.fileName);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CurseAddonFileInformation {
        @JsonProperty
        private String name;

        @JsonProperty
        private String fileName;

        @JsonProperty
        private URL downloadUrl;

        @JsonProperty
        private String[] gameVersion;
    }

	@Override
	public String folderName() {
		return this.folderName;
	}
	
	@Override
	public int forceInject() {
		return inject == null ? 0 : inject.equalsIgnoreCase("true") ? 1 : 2;
	}
}
