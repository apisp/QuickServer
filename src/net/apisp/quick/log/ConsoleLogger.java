package net.apisp.quick.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogger implements Logger {
    private String level;
    private String name;

    public ConsoleLogger(String level, String name) {
        this.level = level;
        this.name = name;
    }

    private void before(String level) {
        System.out.print(
                "[" + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + " " + level + "] " + name + " : ");
    }

    @Override
    public void show(String log, Object... args) {
        before("SHOW");
        System.out.println(String.format(log, args));
    }

    @Override
    public void error(String log, Object... args) {
        if (isErrorEnabled()) {
            before("ERROR");
            System.err.println(String.format(log, args));
        }
    }

    @Override
    public void warn(String log, Object... args) {
        if (isWarnEnabled()) {
            before("WARN");
            System.err.println(String.format(log, args));
        }
    }

    @Override
    public void info(String log, Object... args) {
        if (isInfoEnabled()) {
            before("INFO");
            System.out.println(String.format(log, args));
        }
    }

    @Override
    public void debug(String log, Object... args) {
        if (isDebugEnable()) {
            before("DEBUG");
            System.out.println(String.format(log, args));
        }
    }

    @Override
    public void show(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void error(Throwable e) {
        if (isErrorEnabled()) {
            e.printStackTrace();
        }
    }

    @Override
    public void warn(Throwable e) {
        if (isWarnEnabled()) {
            e.printStackTrace();
        }
    }

    @Override
    public void info(Throwable e) {
        if (isInfoEnabled()) {
            e.printStackTrace();
        }
    }

    @Override
    public void debug(Throwable e) {
        if (isDebugEnable()) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return (level.equalsIgnoreCase(Levels.ERROR) || level.equalsIgnoreCase(Levels.WARN)
                || level.equalsIgnoreCase(Levels.INFO) || level.equalsIgnoreCase(Levels.DEBUG)) ? true : false;
    }

    @Override
    public boolean isWarnEnabled() {
        return (level.equalsIgnoreCase(Levels.WARN) || level.equalsIgnoreCase(Levels.INFO)
                || level.equalsIgnoreCase(Levels.DEBUG)) ? true : false;
    }

    @Override
    public boolean isInfoEnabled() {
        return (level.equalsIgnoreCase(Levels.INFO) || level.equalsIgnoreCase(Levels.DEBUG)) ? true : false;
    }

    @Override
    public boolean isDebugEnable() {
        return (level.equalsIgnoreCase(Levels.DEBUG)) ? true : false;
    }

}
