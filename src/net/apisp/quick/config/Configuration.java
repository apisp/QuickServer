package net.apisp.quick.config;

import java.util.Iterator;

import net.apisp.quick.log.Logger;
import net.apisp.quick.util.Classpaths;
import net.apisp.quick.util.Safes;

public abstract class Configuration {
    private static final Logger LOGGER = Logger.get(Configuration.class);
    private static DefaultConfig configuration = new DefaultConfig();
    static {
        if (Classpaths.existFile("quick.properties")) {
            Configuration tmp = new PropertiesConfig();
            Iterator<KeyValuePair> iterator = tmp.iterator();
            KeyValuePair kv = null;
            while (iterator.hasNext()) { // 覆盖默认配置
                kv = (KeyValuePair) iterator.next();
                configuration.configs.put((String) kv.getKey(),
                        Safes.transform(configuration.configs.get(kv.getKey()), kv.getValue()));
            }
        }
    }

    public static Object get(String key) {
        return configuration.getValue(key);
    }

    public abstract Object getValue(String key);

    public abstract Iterator<KeyValuePair> iterator();

    public static void applySystemArgs(String... args) {
        for (String setting : args) {
            String[] kv = setting.split("=");
            if (kv.length != 2) {
                LOGGER.warn("System args have a unstandard config, discarded!");
                continue;
            }
            configuration.configs.put(kv[0].trim(), Safes.transform(configuration.configs.get(kv[0].trim()), kv[1]));
        }
    }
}
