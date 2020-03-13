package net.jan.moddirector.core.logging;

public interface ModDirectorLogger {
    void log(ModDirectorSeverityLevel level, String domain, String tag, String format, Object... args);
    void logThrowable(ModDirectorSeverityLevel level, String domain, String tag,
                      Throwable throwable, String format, Object... args);
}
