package net.jan.moddirector.core.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RemoteModHash {
    private final String algorithm;
    private final String value;

    @JsonCreator
    public RemoteModHash(
            @JsonProperty(value = "algorithm", required = true) String algorithm,
            @JsonProperty(value = "value", required = true) String value
    ) {
        this.algorithm = algorithm;
        this.value = value;
    }

    public boolean matches(Path file, ModDirector director) {
        try {
            byte[] data = Files.readAllBytes(file);
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] hash = digest.digest(data);
            StringBuilder builder = new StringBuilder(new BigInteger(1, hash).toString(16));
            while(builder.length() < 32) {
                builder.insert(0, '0');
            }

            return builder.toString().equals(value);
        } catch(NoSuchAlgorithmException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/RemoteHash",
                    "CORE", e, "Algorithm %s not provided by JVM, assuming hash matches", algorithm);
        } catch(IOException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/RemoteHash",
                    "CORE", e, "Failed to open %s for hash calculation, assuming hash does not match",
                    file.toString());
        }

        return true;
    }
}
