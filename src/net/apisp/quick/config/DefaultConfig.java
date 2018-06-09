package net.apisp.quick.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.apisp.quick.log.Logger.Levels;

public class DefaultConfig extends Configuration {

    Map<String, Object> configs = new HashMap<>();
    
    public DefaultConfig() {
        configs.put("quick.logging.level", Levels.INFO);
        configs.put("quick.server", "net.apisp.quick.server.low.DefaultServer");
        configs.put("quick.server.port", 8908);
        configs.put("quick.server.threads", 4 * 6);
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
