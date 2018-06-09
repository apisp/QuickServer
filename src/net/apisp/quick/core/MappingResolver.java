/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core;

import java.lang.reflect.Method;
import java.util.Objects;

import net.apisp.quick.annotation.DeleteMapping;
import net.apisp.quick.annotation.GetMapping;
import net.apisp.quick.annotation.PostMapping;
import net.apisp.quick.annotation.PutMapping;
import net.apisp.quick.annotation.ResponseType;
import net.apisp.quick.http.ContentTypes;
import net.apisp.quick.http.HttpMethods;
import net.apisp.quick.log.Logger;

/**
 * 
 *
 * @author jing_ykun 
 * 2018年6月8日 上午11:46:34
 */
public class MappingResolver {
	private static Logger LOGGER = Logger.get(MappingResolver.class);
	private static MappingResolver instance;
	private Class<?>[] classes;

	public MappingResolver() {

	}
	
	public void resolve(ServerContext serverContext) {
		Class<?> clazz;
		Method method;
		GetMapping getMapping;
		PostMapping postMaping;
		PutMapping putMapping;
		DeleteMapping deleteMapping;
		ResponseType responseType;
		String mappingKey;
		for (int j = 0; j < classes.length; j++) {
			clazz = classes[j];
			Method[] methods = clazz.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				method = methods[i];
				getMapping = method.getAnnotation(GetMapping.class);
				postMaping = method.getAnnotation(PostMapping.class);
				putMapping = method.getAnnotation(PutMapping.class);
				deleteMapping = method.getAnnotation(DeleteMapping.class);
				responseType = method.getAnnotation(ResponseType.class);

				byte hmf = 0;
				if (Objects.nonNull(getMapping) || Objects.nonNull(postMaping) && ((hmf = 1) == 1)
						|| Objects.nonNull(putMapping) && ((hmf = 2) == 2)
						|| Objects.nonNull(deleteMapping) && ((hmf = 3) == 3)) {
					String httpMethod = HttpMethods.GET;
					String uri = null;
					switch (hmf) {
					case 1:
						httpMethod = HttpMethods.POST;
						uri = postMaping.value();
						break;
					case 2:
						httpMethod = HttpMethods.PUT;
						uri = putMapping.value();
						break;
					case 3:
						httpMethod = HttpMethods.DELETE;
						uri = deleteMapping.value();
						break;
					default:
						httpMethod = HttpMethods.GET;
						uri = getMapping.value();
						break;
					}
					mappingKey = httpMethod + " " + uri.trim();
					try {
						serverContext.mapping(mappingKey, new RequestExecutorInfo(method, clazz.newInstance(),
								responseType != null ? responseType.value() : ContentTypes.JSON));
						LOGGER.info("Mapping %s : %s", mappingKey, method.toGenericString());
					} catch (InstantiationException | IllegalAccessException e) {
						LOGGER.error("控制器类需要无参数构造！");
					}
				}
			}
		}
	}

	/**
	 * 准备映射
	 *
	 * @param classes 
	 */
	public static synchronized MappingResolver prepare(Class<?>[] classes) {
		if(instance == null) {
			instance = new MappingResolver();
		}
		instance.classes = classes;
		return instance;
	}

}
