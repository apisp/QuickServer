/**
 * Copyright (c) 2018 Ujued and APISP.NET. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.apisp.quick.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import org.json.JSONObject;

/**
 * 实验性质的工具
 * 
 * @author Ujued
 * @date 2018-06-22 18:18:58
 */
public abstract class Quicks {
	public static String packageName(Class<?> bootClass) {
		if (Objects.isNull(bootClass) || bootClass.getName().indexOf('.') == -1) {
			return "";
		}
		return bootClass.getName().substring(0, bootClass.getName().lastIndexOf('.'));
	}

	/**
	 * 根据URI获取合适的Path。目前仅支持系统FS和ZIPFS
	 * 
	 * @param uri
	 * @return
	 */
	public static Path tactfulPath(URI uri) {
		if (uri.getScheme().equals("jar")) {
			int index = uri.toString().indexOf('!') + 1;
			URI rootUri = URI.create(uri.toString().substring(0, index + 1));
			String filePath = uri.toString().substring(index, uri.toString().length());
			try {
				FileSystem zipfs = FileSystems.newFileSystem(rootUri, new HashMap<>());
				return zipfs.getPath(filePath);
			} catch (FileSystemAlreadyExistsException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Paths.get(uri);
	}

	/**
	 * 将${value}转换为${model}的类型
	 * 
	 * @param model
	 * @param value
	 * @return
	 */
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
			} catch (ParseException e) {
			}
		}
		return value;
	}

	/**
	 * 获取Quick Server 版本号
	 * 
	 * @return
	 */
	public static String version() {
		String url = Quicks.class.getResource(Quicks.class.getSimpleName() + ".class").toString();
		int i = -1;
		if ((i = url.indexOf('!')) != -1) {
			String name = url.substring(0, i);
			if (name.indexOf('-') == -1) {
				return "unkonwn";
			}
			return name.substring(name.lastIndexOf('-') + 1, name.lastIndexOf('.'));
		}
		return "unkonwn";
	}

	/**
	 * 统一消息
	 * 
	 * @param status  状态码
	 * @param message 消息
	 * @param data    业务数据
	 * @param ext     附加对象
	 * @return
	 */
	public static JSONObject message(int status, String message, JSONObject data, Object ext) {
		JSONObject root = new JSONObject();
		root.put("status", status);
		root.put("message", message);
		root.put("ext", ext);
		root.put("data", data);
		return root;
	}

	/**
	 * 智能转换字符串为其他合适类型
	 * 
	 * @param obj
	 * @return
	 */
	public static Object intelliConvert(String obj) {

		if (obj.equals("true")) {
			return Boolean.TRUE;
		}
		if (obj.equals("false")) {
			return Boolean.FALSE;
		}
		try {
			return Integer.valueOf(obj);
		} catch (NumberFormatException e) {
		}
		try {
			return Double.valueOf(obj);
		} catch (NumberFormatException e) {
		}
		try {
			return Long.valueOf(obj);
		} catch (NumberFormatException e) {
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.parse(obj.toString());
		} catch (ParseException e) {
		}
		return obj;
	}
}
