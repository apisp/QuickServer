package net.apisp.quick.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.apisp.quick.util.Strings;

public class ConsoleLog implements Log {
    private String level;
    private String name;

    public ConsoleLog() {
    }

    public ConsoleLog(String level, String name) {
        this.level = level;
        this.name = name;
    }

    private String before(String level) {
        return String.format(new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + " %6s [%11s] %-41s <| ", level,
                Thread.currentThread().getName(), name);
    }

    @Override
    public void show(String log, Object... args) {
        System.out.println(before("SHOW") + Strings.template(log, args));
    }

    @Override
    public void error(String log, Object... args) {
        if (isErrorEnabled()) {
            System.err.println(before(Levels.ERROR) + Strings.template(log, args));
        }
    }

    @Override
    public void warn(String log, Object... args) {
        if (isWarnEnabled()) {
            System.err.println(before(Levels.WARN) + Strings.template(log, args));
        }
    }

    @Override
    public void info(String log, Object... args) {
        if (isInfoEnabled()) {
            System.out.println(before(Levels.INFO) + Strings.template(log, args));
        }
    }

    @Override
    public void debug(String log, Object... args) {
        if (isDebugEnable()) {
            System.out.println(before(Levels.DEBUG) + Strings.template(log, args));
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

    @Override
    public void log(String level, String log, Object... args) {
        if (this.level.equals(level)) {
            System.out.println(before(level) + Strings.template(log, args));
        }
    }

    @Override
    public void log(String level, Throwable e) {
        if (this.level.equals(level)) {
            e.printStackTrace();
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public Log normalize() {
        return this;
    }

}
