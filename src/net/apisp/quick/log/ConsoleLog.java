package net.apisp.quick.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLog implements Log {
    private String level;
    private String name;

    public ConsoleLog(String level, String name) {
        this.level = level;
        this.name = name;
    }

    private String before() {
        return new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + " %6s [%10s] %-42s <|  ";
    }

    @Override
    public void show(String log, Object... args) {
        byte add = 3;
        Object[] params = new Object[args.length + add];
        params[0] = "SHOW";
        params[1] = Thread.currentThread().getName();
        params[2] = name;
        for (int i = 0; i < args.length; i++) {
            params[i + add] = args[i];
        }
        System.out.printf(before() + log + "\n", params);
    }

    @Override
    public void error(String log, Object... args) {
        if (isErrorEnabled()) {
            byte add = 3;
            Object[] params = new Object[args.length + add];
            params[0] = Levels.ERROR;
            params[1] = Thread.currentThread().getName();
            params[2] = name;
            for (int i = 0; i < args.length; i++) {
                params[i + add] = args[i];
            }
            System.err.printf(before() + log + "\n", params);
        }
    }

    @Override
    public void warn(String log, Object... args) {
        if (isWarnEnabled()) {
            byte add = 3;
            Object[] params = new Object[args.length + add];
            params[0] = Levels.WARN;
            params[1] = Thread.currentThread().getName();
            params[2] = name;
            for (int i = 0; i < args.length; i++) {
                params[i + add] = args[i];
            }
            System.err.printf(before() + log + "\n", params);
        }
    }

    @Override
    public void info(String log, Object... args) {
        if (isInfoEnabled()) {
            byte add = 3;
            Object[] params = new Object[args.length + add];
            params[0] = Levels.INFO;
            params[1] = Thread.currentThread().getName();
            params[2] = name;
            for (int i = 0; i < args.length; i++) {
                params[i + add] = args[i];
            }
            System.out.printf(before() + log + "\n", params);
        }
    }

    @Override
    public void debug(String log, Object... args) {
        if (isDebugEnable()) {
            byte add = 3;
            Object[] params = new Object[args.length + add];
            params[0] = Levels.DEBUG;
            params[1] = Thread.currentThread().getName();
            params[2] = name;
            for (int i = 0; i < args.length; i++) {
                params[i + add] = args[i];
            }
            System.out.printf(before() + log + "\n", params);
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
            byte add = 3;
            Object[] params = new Object[args.length + add];
            params[0] = level;
            params[1] = Thread.currentThread().getName();
            params[2] = name;
            for (int i = 0; i < args.length; i++) {
                params[i + add] = args[i];
            }
            System.out.printf(before() + log + "\n", params);
        }
    }

    @Override
    public void log(String level, Throwable e) {
        if (this.level.equals(level)) {
            e.printStackTrace();
        }
    }

}
