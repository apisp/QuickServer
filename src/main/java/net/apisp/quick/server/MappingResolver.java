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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import net.apisp.quick.core.QuickContext;
import net.apisp.quick.core.annotation.EnableCros;
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
import net.apisp.quick.util.Reflects;

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
    private QuickContext quickContext;

    public MappingResolver() {
    }

    private void prepare() {
        Scanning scanning = bootClass.getAnnotation(Scanning.class);
        EnableCros crossDomain = bootClass.getAnnotation(EnableCros.class);
        if (Objects.nonNull(scanning)) {
            for (Class<?> cls : scanning.value()) {
                controllerClasses.add(cls);
            }
        }
        if (Objects.nonNull(crossDomain)) {
            quickContext.responseHeaders().put("Access-Control-Allow-Origin", "*");
            quickContext.responseHeaders().put("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PUT,HEAD");
            quickContext.responseHeaders().put("Access-Control-Allow-Headers", "x-requested-with");
            Reflects.invoke(quickContext, "setCrossDomain", true);
        }
        controllerClasses.add(bootClass);
    }

    public MappingResolver addControllerClasses(Class<?>[] classes) {
        if (Objects.isNull(classes)) {
            return this;
        }
        for (int i = 0; i < classes.length; i++) {
            this.controllerClasses.add(classes[i]);
        }
        return this;
    }

    /**
     * 开始映射到ServerContext
     */
    public void resolve() {
        Object controller = null;
        Iterator<Class<?>> controllerIter = controllerClasses.iterator();
        while (controllerIter.hasNext()) {
            Class<?> clazz = (Class<?>) controllerIter.next();
            // 未缓存的控制器对象，创建并缓存
            if (quickContext.contains(clazz)) {
                continue;
            }
            try {
                // 单例对象自动注入到Controller
                controller = Injections.inject(clazz.newInstance(), quickContext);
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.error("控制器类需要无参数构造！");
            }

            // Controller 域下所有API可能需要跨域
            boolean shouldSetCrossDomain = false;
            if (!quickContext.isCors() && clazz.getAnnotation(EnableCros.class) != null) {
                shouldSetCrossDomain = true;
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue; // 抛弃非共有方法
                }
                Get getMapping = method.getAnnotation(Get.class);
                Post postMaping = method.getAnnotation(Post.class);
                Put putMapping = method.getAnnotation(Put.class);
                Delete deleteMapping = method.getAnnotation(Delete.class);

                ResponseType responseType = method.getAnnotation(ResponseType.class);
                View view = method.getAnnotation(View.class);

                EnableCros crossDomain = null;
                // 上下文设置了跨域，就不需要检查
                if (!quickContext.isCors()) {
                    crossDomain = method.getAnnotation(EnableCros.class);
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
                    String mappingKey = httpMethod + " " + uri.trim();

                    RequestExecutorInfo info = new RequestExecutorInfo(method, controller);

                    // 默认响应类型
                    info.addHeader("Content-Type", ContentTypes.JSON + "; charset=" + quickContext.charset());

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
                        info.addHeader("Content-Type", ContentTypes.HTML + "; charset=" + quickContext.charset());
                    }

                    // 设置指定的响应类型
                    if (responseType != null) {
                        info.addHeader("Content-Type", responseType.value() + "; charset=" + quickContext.charset());
                    }

                    // 跨域设置
                    if (!quickContext.isCors() && (shouldSetCrossDomain || crossDomain != null)) {
                        info.addHeader("Access-Control-Allow-Origin", "*");
                        info.addHeader("Access-Control-Allow-Headers", "x-requested-with");
                        info.addHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PUT,HEAD");
                    }
                    quickContext.mapping(mappingKey, info);
                }
            }

            // Mapping 完成，缓存Controller实例
            quickContext.accept(controller);
        }
        // 指定的Controller类Mapping完毕，清空
        controllerClasses.clear();
    }

    /**
     * 准备URI与业务逻辑函数的映射
     *
     * @param classes
     */
    public static synchronized MappingResolver prepare(Class<?> bootClass, QuickContext context) {
        if (instance == null) {
            instance = new MappingResolver();
            instance.bootClass = bootClass;
            instance.quickContext = context;
            instance.prepare();
        }
        return instance;
    }
}
