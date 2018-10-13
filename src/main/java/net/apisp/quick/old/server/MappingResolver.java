/**
 * Copyright (c) 2018 Ujued and APISP.NET. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.apisp.quick.old.server;

import net.apisp.quick.core.criterion.http.annotation.*;
import net.apisp.quick.core.criterion.MimeTypes;
import net.apisp.quick.core.criterion.http.constant.HttpMethods;
import net.apisp.quick.ioc.Injections;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.old.server.std.QuickContext;
import net.apisp.quick.support.lang.FlowControl;
import net.apisp.quick.util.Reflects;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 映射决策
 *
 * @author ujued
 * @date 2018-06-08 11:46:34
 */
public class MappingResolver {
    private static Log LOG = LogFactory.getLog(MappingResolver.class);
    private static MappingResolver instance;
    private Set<Class<?>> controllerClassSet = new HashSet<>();

    private MappingResolver(Class<?>[] controllerClasses) {
        Collections.addAll(controllerClassSet, controllerClasses);
    }

    /**
     * 添加控制器类
     *
     * @param classes
     * @return
     */
    public MappingResolver addControllerClasses(Class<?>... classes) {
        if (Objects.isNull(classes)) {
            return this;
        }
        Collections.addAll(controllerClassSet, classes);
        return this;
    }

    /**
     * 开始映射到ServerContext
     */
    public void resolveTo(QuickContext quickContext) {
        Object controller = null;
        Iterator<Class<?>> controllerIter = controllerClassSet.iterator();
        while (controllerIter.hasNext()) {
            Class<?> clazz = controllerIter.next();
            // 已缓存跳过
            if (quickContext.contains(clazz)) {
                continue;
            }
            // 未缓存的控制器对象，创建并缓存
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
                Post postMapping = method.getAnnotation(Post.class);
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
                if (Objects.nonNull(getMapping) || Objects.nonNull(postMapping) && ((hmf = 1) == 1)
                        || Objects.nonNull(putMapping) && ((hmf = 2) == 2)
                        || Objects.nonNull(deleteMapping) && ((hmf = 3) == 3)) {
                    String httpMethod;
                    String uri;
                    switch (hmf) {
                        case 1:
                            httpMethod = HttpMethods.POST;
                            uri = postMapping.value();
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
                    info.addHeader("Content-Type", MimeTypes.JSON + "; charset=" + quickContext.charset());

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
                        info.addHeader("Content-Type", MimeTypes.HTML + "; charset=" + quickContext.charset());
                    }

                    // 设置指定的响应类型
                    if (responseType != null) {
                        info.addHeader("Content-Type", responseType.value() + "; charset=" + quickContext.charset());
                        FlowControl.get().when(responseType.value().equals(MimeTypes.STREAM)).run(() -> {
                            Reflects.invoke(info, "setType", RequestExecutorInfo.TYPE_STREAM);
                        });
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
        controllerClassSet.clear();
    }

    /**
     * 准备URI与业务逻辑函数的映射
     *
     * @param controllerClasses
     * @return
     */
    public static synchronized MappingResolver prepare(Class<?>[] controllerClasses) {
        if (instance == null) {
            instance = new MappingResolver(controllerClasses);
        }
        return instance;
    }
}
