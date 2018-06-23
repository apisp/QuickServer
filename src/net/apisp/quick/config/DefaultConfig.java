package net.apisp.quick.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.apisp.quick.log.Log.Levels;

public class DefaultConfig extends Configuration {

    Map<String, Object> configs = new HashMap<>();

    public DefaultConfig() {
        configs.put("charset", "UTF-8");
        configs.put("logging.class", "net.apisp.quick.log.ConsoleLog");
        configs.put("logging.level", Levels.INFO);
        configs.put("server", "net.apisp.quick.server.DefaultQuickServer");
        configs.put("server.port", 8908);
        configs.put("server.threads", 4 * 6);
        configs.put("server.tmp.dir", System.getProperty("user.dir"));
    }

    @Override
    public Object getValue(String key) {
        return configs.get(key);
    }

    @Override
    public Iterator<KeyValuePair> iterator() {
        return null;
    }

}
