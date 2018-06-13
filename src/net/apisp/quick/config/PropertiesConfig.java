package net.apisp.quick.config;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class PropertiesConfig extends Configuration {
    private Properties properties;
    
    public PropertiesConfig() {
        properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/quick.properties"));
        } catch (Throwable e) {
            new IllegalAccessException("未在classpath找到配置文件quick.properties");
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
