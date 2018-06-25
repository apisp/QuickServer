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
package net.apisp.quick.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import net.apisp.quick.core.annotation.CrossDomain;
import net.apisp.quick.core.annotation.Delete;
import net.apisp.quick.core.annotation.Get;
import net.apisp.quick.core.annotation.Post;
import net.apisp.quick.core.annotation.Put;
import net.apisp.quick.core.annotation.ResponseType;
import net.apisp.quick.core.annotation.Scanning;
import net.apisp.quick.core.annotation.View;
import net.apisp.quick.core.http.ContentTypes;
import net.apisp.quick.core.http.HttpMethods;
import net.apisp.quick.ioc.Container.Injections;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.util.Quicks;

/**
 * 映射决策
 * 
 * @author UJUED
 * @date 2018-06-08 11:46:34
 */
public class MappingResolver {
    private static Log LOG = LogFactory.getLog(MappingResolver.class);
    private static MappingResolver instance;
    private Class<?> bootClass;
    private Set<Class<?>> controllerClasses = new HashSet<>();
    private ServerContext serverContext;

    public MappingResolver() {
    }

    private void prepare() {
        Annotation[] annos = bootClass.getAnnotations();
        for (int i = 0; i < annos.length; i++) {
            if (annos[i] instanceof Scanning) {
                Class<?>[] cls = ((Scanning) annos[i]).value();
                for (int j = 0; j < cls.length; j++) {
                    controllerClasses.add(cls[j]);
                }
                continue;
            } else if (annos[i] instanceof CrossDomain) {
                serverContext.responseHeaders().put("Access-Control-Allow-Origin", "*");
                serverContext.responseHeaders().put("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PUT,HEAD");
                serverContext.responseHeaders().put("Access-Control-Allow-Headers", "x-requested-with");
                Quicks.invoke(serverContext, "setCrossDomain", true);
                continue;
            }
        }
        controllerClasses.add(bootClass);
    }

    public MappingResolver addControllerClasses(Class<?>[] classes) {
        for (int i = 0; i < classes.length; i++) {
            this.controllerClasses.add(classes[i]);
        }
        return this;
    }

    /**
     * 开始映射到ServerContext
     */
    public void resolve() {
        Class<?> clazz;
        Method method;
        Get getMapping = null;
        Post postMaping = null;
        Put putMapping = null;
        Delete deleteMapping = null;

        ResponseType responseType = null;
        View view = null;
        CrossDomain crossDomain = null;

        String mappingKey = null;
        Object controller = null;
        Iterator<Class<?>> controllerIter = controllerClasses.iterator();
        while (controllerIter.hasNext()) {
            clazz = (Class<?>) controllerIter.next();
            // 创建单例控制器对象，并缓存到WebContext
            try {
                controller = clazz.newInstance();
                Injections.inject(controller, serverContext); // 单例对象自动注入Controller
                serverContext.accept(controller);
            } catch (InstantiationException | IllegalAccessException e1) {
                LOG.error("控制器类需要无参数构造！");
            }

            // Controller 域下所有API可能需要跨域
            boolean shouldSetCrossDomain = false;
            if (!serverContext.isCrossDomain() && clazz.getAnnotation(CrossDomain.class) != null) {
                shouldSetCrossDomain = true;
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                method = methods[i];
                getMapping = method.getAnnotation(Get.class);
                postMaping = method.getAnnotation(Post.class);
                putMapping = method.getAnnotation(Put.class);
                deleteMapping = method.getAnnotation(Delete.class);

                responseType = method.getAnnotation(ResponseType.class);
                view = method.getAnnotation(View.class);

                // 上下文设置了跨域，就不需要检查
                if (!serverContext.isCrossDomain()) {
                    crossDomain = method.getAnnotation(CrossDomain.class);
                }
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

                    RequestExecutorInfo info = new RequestExecutorInfo(method, controller);

                    // 默认响应类型
                    info.addHeader("Content-Type", ContentTypes.JSON + "; charset=" + serverContext.charset());
                    
                    // 视图方式响应
                    if (view != null) {
                        StringBuilder dir = new StringBuilder(view.value());
                        while (true) {
                            if (dir.length() > 0 && dir.charAt(0) == '/') {
                                dir.deleteCharAt(0);
                            } else {
                                break;
                            }
                        }
                        if (dir.length() > 0 && dir.charAt(dir.length() - 1) != '/') {
                            dir.append('/');
                        }
                        info.setViewDirectory(dir.toString());
                        info.addHeader("Content-Type", ContentTypes.HTML + "; charset=" + serverContext.charset());
                    }

                    // 设置指定的响应类型
                    if (responseType != null) {
                        info.addHeader("Content-Type", responseType.value() + "; charset=" + serverContext.charset());
                    }

                    // 跨域设置
                    if (!serverContext.isCrossDomain() && (shouldSetCrossDomain || crossDomain != null)) {
                        info.addHeader("Access-Control-Allow-Origin", "*");
                        info.addHeader("Access-Control-Allow-Headers", "x-requested-with");
                        info.addHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PUT,HEAD");
                    }
                    serverContext.mapping(mappingKey, info);
                }
            }
        }
    }

    /**
     * 准备URI与业务逻辑函数的映射
     *
     * @param classes
     */
    public static synchronized MappingResolver prepare(Class<?> bootClass, ServerContext context) {
        if (instance == null) {
            instance = new MappingResolver();
            instance.bootClass = bootClass;
            instance.serverContext = context;
            instance.prepare();
        }
        return instance;
    }

}
