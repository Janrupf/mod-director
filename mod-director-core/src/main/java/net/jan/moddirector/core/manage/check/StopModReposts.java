package net.jan.moddirector.core.manage.check;

import com.fasterxml.jackson.databind.JavaType;
import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.configuration.ConfigurationController;
import net.jan.moddirector.core.exception.ModDirectorException;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.manage.ModDirectorError;
import net.jan.moddirector.core.util.WebClient;
import net.jan.moddirector.core.util.WebGetResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StopModReposts {
    private static final List<StopModRepostsEntry> ENTRIES = new ArrayList<>();
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Object INITIALIZATION_LOCK = new Object();

    public static void check(ModDirector director, URL url) throws ModDirectorException {
        if(!INITIALIZED.get()) {
            synchronized(INITIALIZATION_LOCK) {
                if(!INITIALIZED.get()) {
                    initialize(director);
                }
            }
        }

        director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "StopModReposts", "CORE",
                "Checking %s against StopModReposts database", url.toExternalForm());
        for(StopModRepostsEntry entry : ENTRIES) {
            if(url.toExternalForm().contains(entry.domain()) ) {
                director.getLogger().log(ModDirectorSeverityLevel.ERROR, "StopModReposts", "CORE",
                        "STOP! Download URL %s is flagged in StopModReposts database, ABORTING!",
                        url.toExternalForm());
                director.getLogger().log(ModDirectorSeverityLevel.ERROR, "StopModReposts", "CORE",
                        "Domain %s is flagged", entry.domain());
                director.getLogger().log(ModDirectorSeverityLevel.ERROR, "StopModReposts", "CORE",
                        "Reason: ", entry.reason());
                if(!entry.notes().isEmpty()) {
                    director.getLogger().log(ModDirectorSeverityLevel.ERROR, "StopModReposts", "CORE",
                            "Notes: ", entry.notes());
                }
                director.addError(new ModDirectorError(ModDirectorSeverityLevel.ERROR,
                        "Found URL " + url.toExternalForm() + " on domain " + entry.domain() + " flagged " +
                                "in the StopModReposts database! Please use legal download pages, " +
                                "ModDirector has aborted the launch."));
                throw new ModDirectorException("Found flagged URL " + url.toExternalForm() +
                        " in StopModReposts database");
            }
        }
    }

    private static void initialize(ModDirector director) throws ModDirectorException {
        director.getLogger().log(ModDirectorSeverityLevel.DEBUG, "StopModReposts", "CORE",
                "Initializing StopModReposts module");

        try(WebGetResponse response =
                    WebClient.get(new URL("https://api.stopmodreposts.org/sites.json"))) {
            JavaType targetType = ConfigurationController.OBJECT_MAPPER.getTypeFactory().
                    constructCollectionType(List.class, StopModRepostsEntry.class);

            ENTRIES.addAll(ConfigurationController.OBJECT_MAPPER.readValue(response.getInputStream(), targetType));
        } catch(MalformedURLException e) {
            throw new RuntimeException(
                    "https://api.stopmodreposts.org/sites.json seems to be an invalid URL?", e);
        } catch(IOException e) {
            throw new ModDirectorException("Failed to retrieve StopModReposts database", e);
        }

        INITIALIZED.set(true);
    }
}
