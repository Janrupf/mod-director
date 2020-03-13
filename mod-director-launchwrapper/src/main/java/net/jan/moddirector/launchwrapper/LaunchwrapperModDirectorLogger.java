package net.jan.moddirector.launchwrapper;

import net.jan.moddirector.core.logging.ModDirectorLogger;
import net.jan.moddirector.core.logging.ModDirectorSeverityLevel;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;

public class LaunchwrapperModDirectorLogger implements ModDirectorLogger {
    @Override
    public void log(ModDirectorSeverityLevel level, String domain, String tag, String format, Object... args) {
        LogWrapper.log(domain + "[" + tag + "]", toLog4jLevel(level), format, args);
    }

    @Override
    public void logThrowable(ModDirectorSeverityLevel level, String domain, String tag, Throwable throwable, String format, Object... args) {
        LogWrapper.log(domain + "[" + tag + "]", toLog4jLevel(level), throwable, format, args);
    }

    private Level toLog4jLevel(ModDirectorSeverityLevel level) {
        switch(level) {
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            default:
                throw new AssertionError("UNREACHABLE");
        }
    }
}
