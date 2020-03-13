package net.jan.moddirector.core.manage;

import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;

public class ModDirectorError {
    private final ModDirectorSeverityLevel level;
    private final String message;
    private final Throwable exception;

    public ModDirectorError(ModDirectorSeverityLevel level, String message) {
        this.level = level;
        this.message = message;
        this.exception = null;
    }

    public ModDirectorError(ModDirectorSeverityLevel level, Throwable exception) {
        this.level = level;
        this.message = exception.getMessage();
        this.exception = exception;
    }

    public ModDirectorError(ModDirectorSeverityLevel level, String message, Throwable exception) {
        this.level = level;
        this.message = message;
        this.exception = exception;
    }

    public ModDirectorSeverityLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }
}
