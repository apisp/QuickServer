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
        System.out.print("[" + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + " " + level + "] " + name
                + " : ");
    }

    @Override
    public void show(String log, Object... args) {
        before("SHOW");
        System.out.println(String.format(log, args));
    }

    @Override
    public void error(String log, Object... args) {
        if (level.equalsIgnoreCase(Levels.INFO) || level.equalsIgnoreCase(Levels.WARN)
                || level.equalsIgnoreCase(Levels.ERROR)) {
            before("ERROR");
            System.out.println(String.format(log, args));
        }
    }

    @Override
    public void warn(String log, Object... args) {
        if (level.equalsIgnoreCase(Levels.INFO) || level.equalsIgnoreCase(Levels.WARN)) {
            before("WARN");
            System.out.println(String.format(log, args));
        }
    }

    @Override
    public void info(String log, Object... args) {
        if (level.equalsIgnoreCase(Levels.INFO)) {
            before("INFO");
            System.out.println(String.format(log, args));
        }
    }

    @Override
    public void debug(String log, Object... args) {
        if (level.equalsIgnoreCase(Levels.DEBUG)) {
            before("DEBUG");
            System.out.println(String.format(log, args));
        }
    }

}
