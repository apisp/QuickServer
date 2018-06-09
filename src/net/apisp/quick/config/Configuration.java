package net.apisp.quick.config;

import java.util.Iterator;

import net.apisp.quick.util.Classpaths;
import net.apisp.quick.util.Safes;

public abstract class Configuration {
	private static DefaultConfig configuration = new DefaultConfig();
	static {
		if (Classpaths.existFile("application.properties")) {
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
}
