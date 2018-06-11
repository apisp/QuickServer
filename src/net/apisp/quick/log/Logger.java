package net.apisp.quick.log;

import net.apisp.quick.config.Configuration;

public interface Logger {
    public static Logger get(Class<?> clazz) {
        // TODO chose a Logger
        return new ConsoleLogger((String) Configuration.get("logging.level"), clazz.getName());
    }

    public static final class Levels {
        public static String ERROR = "ERROR";
        public static String WARN = "WARN";
        public static String INFO = "INFO";
        public static String DEBUG = "DEBUG";
    }

    void show(String log, Object... args);

    void error(String log, Object... args);

    void warn(String log, Object... args);

    void info(String log, Object... args);

    void debug(String log, Object... args);

    void show(Throwable e);

    void error(Throwable e);

    void warn(Throwable e);

    void info(Throwable e);

    void debug(Throwable e);

    boolean isErrorEnabled();

    boolean isWarnEnabled();

    boolean isInfoEnabled();

    boolean isDebugEnable();

}
