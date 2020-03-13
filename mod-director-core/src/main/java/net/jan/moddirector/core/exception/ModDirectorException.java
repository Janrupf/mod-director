package net.jan.moddirector.core.exception;

public class ModDirectorException extends Exception {
    public ModDirectorException(String message) {
        super(message);
    }

    public ModDirectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModDirectorException(Throwable cause) {
        super(cause);
    }
}
