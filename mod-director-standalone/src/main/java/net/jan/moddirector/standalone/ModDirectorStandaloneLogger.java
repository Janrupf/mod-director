package net.jan.moddirector.standalone;

import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.jan.moddirector.core.logging.ModDirectorLogger;

public class ModDirectorStandaloneLogger implements ModDirectorLogger {
    @Override
    public void log(ModDirectorSeverityLevel level, String domain, String tag, String format, Object... args) {
        synchronized(System.out) {
            // TODO: Make better
            System.out.printf("[%s(%s):%s] %s\n", domain, tag, level.name(), String.format(format, args));
        }
    }

    @Override
    public void logThrowable(ModDirectorSeverityLevel level, String domain, String tag, Throwable throwable,
                             String format, Object... args) {
        synchronized(System.out) {
            // TODO: Make better
            System.out.printf("[%s(%s):%s] %s\n", domain, tag, level.name(), String.format(format, args));
            throwable.printStackTrace();
        }
    }
}
