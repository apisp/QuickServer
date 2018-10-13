package net.apisp.quick.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.apisp.quick.log.Log.Levels;

public class DefaultConfig extends Configuration {

    Map<String, Object> configs = new HashMap<>();

    public DefaultConfig() {
        configs.put("charset", "UTF-8");
        configs.put("exception.handler", "net.apisp.quick.support.BuiltinHttpServerExceptionHandler");
        configs.put("logging.class", "net.apisp.quick.log.ConsoleLog");
        configs.put("logging.level", Levels.INFO);
        configs.put("server", "net.apisp.quick.old.server.http.DefaultQuickServer");
        configs.put("server.port", 8908);
        configs.put("server.threads", 4 * 6);
        configs.put("server.tmp.dir", System.getProperty("user.dir"));
        configs.put("support.access.key", "1234560");
        configs.put("support.access.open", true);
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
