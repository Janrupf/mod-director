package net.jan.moddirector.core.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jan.moddirector.core.ModDirector;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.platform.PlatformSide;
import net.jan.moddirector.core.util.HashResult;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RemoteModMetadata {
    private final Map<String, String> hashes;
    private final PlatformSide side;

    @JsonCreator
    public RemoteModMetadata(
            @JsonProperty(value = "hash") LinkedHashMap<String, String> hashes,
            @JsonProperty(value = "side") PlatformSide side
    ) {
        this.hashes = hashes;
        this.side = side;
    }

    public HashResult checkHashes(Path file, ModDirector director) {
        if(hashes == null) {
            return HashResult.UNKNOWN;
        }

        byte[] data;

        try {
            data = Files.readAllBytes(file);
        } catch(IOException e) {
            director.getLogger().logThrowable(ModDirectorSeverityLevel.WARN, "ModDirector/RemoteHash",
                    "CORE", e, "Failed to open %s for hash calculation, assuming hash does not match",
                    file.toString());
            return HashResult.UNMATCHED;
        }

        StringBuilder hashBuilder = new StringBuilder();
        for(Map.Entry<String, String> hashEntry : hashes.entrySet()) {
            hashBuilder.setLength(0);

            try {
                MessageDigest digest = MessageDigest.getInstance(hashEntry.getKey());

                byte[] hash = digest.digest(data);
                hashBuilder.append(new BigInteger(1, hash).toString(16));
                while(hashBuilder.length() < 32) {
                    hashBuilder.insert(0, '0');
                }

                if(!hashBuilder.toString().equals(hashEntry.getValue())) {
                    return HashResult.UNMATCHED;
                } else {
                    return HashResult.MATCHED;
                }
            } catch(NoSuchAlgorithmException e) {
                director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/RemoteModMetadata",
                        "CORE", "Hash algorithm %s not supported by JVM", hashEntry.getKey());
            }
        }

        director.getLogger().log(ModDirectorSeverityLevel.WARN, "ModDirector/RemoteModMetadata",
                "CORE", "All given hash algorithms are not supported by the JVM");
        return HashResult.UNKNOWN;
    }

    public boolean shouldTryInstall(ModDirector modDirector) {
        PlatformSide currentSide = modDirector.getPlatform().side();
        return currentSide == null || side == null || currentSide == side;
    }
}
