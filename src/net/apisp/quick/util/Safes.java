package net.apisp.quick.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Safes {
	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadClass(String classname, Class<T> clazz) {
		Class<T> rClass = null;
		try {
			Class<?> lClass = Safes.class.getClassLoader().loadClass(classname);
			try {
				rClass = (Class<T>) lClass;
			} catch (Throwable e) {
				return null;
			}
		} catch (ClassNotFoundException e) {
			return null;
		}
		return rClass;
	}

	public static Object transform(Object model, Object value) {
		if (model instanceof Integer) {
			return Integer.valueOf(value.toString());
		} else if (model instanceof String) {
			return value.toString();
		} else if (model instanceof Double) {
			return Double.valueOf(value.toString());
		} else if (model instanceof Boolean) {
			return Boolean.valueOf(value.toString());
		} else if (model instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				return sdf.parse(value.toString());
			} catch (ParseException e) {}
		}
		return value;
	}
}
