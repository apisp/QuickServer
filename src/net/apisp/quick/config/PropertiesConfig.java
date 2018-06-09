package net.apisp.quick.config;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.apisp.quick.log.Logger;

public class PropertiesConfig extends Configuration {

    private static final Logger LOGGER = Logger.get(PropertiesConfig.class);
    private Properties properties;
    
    public PropertiesConfig() {
        properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/application.properties"));
        } catch (Throwable e) {
            LOGGER.error("未在classpath找到配置文件application.properties");
        }
    }
    @Override
    public Object getValue(String key) {
        return properties.get(key);
    }
    @Override
    public Iterator<KeyValuePair> iterator() {
        Iterator<Entry<Object, Object>> configs = properties.entrySet().iterator();
        Set<KeyValuePair> kvs = new HashSet<>();
        Entry<Object, Object> kv = null;
        while (configs.hasNext()) {
            kv = configs.next();
            kvs.add(new KeyValuePair(kv.getKey(), kv.getValue()));
        }
        return kvs.iterator();
    }

}
