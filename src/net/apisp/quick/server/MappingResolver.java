/**
 * Copyright 2018-present, APISP.NET.
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
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import net.apisp.quick.annotation.CrossDomain;
import net.apisp.quick.annotation.DeleteMapping;
import net.apisp.quick.annotation.GetMapping;
import net.apisp.quick.annotation.PostMapping;
import net.apisp.quick.annotation.PutMapping;
import net.apisp.quick.annotation.ResponseType;
import net.apisp.quick.annotation.Scanning;
import net.apisp.quick.core.http.ContentTypes;
import net.apisp.quick.core.http.HttpMethods;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.RequestProcessor.RequestExecutorInfo;
import net.apisp.quick.server.var.ServerContext;

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
    private Class<?>[] classes;
    private ServerContext serverContext;

    public MappingResolver() {
    }

    private void prepare() {
        Annotation[] annos = bootClass.getAnnotations();
        for (int i = 0; i < annos.length; i++) {
            if (annos[i] instanceof Scanning) {
                Class<?>[] cls = ((Scanning) annos[i]).value();
                this.classes = Arrays.copyOf(cls, cls.length + 1);
                continue;
            } else if (annos[i] instanceof CrossDomain) {
                serverContext.getDefaultRespHeaders().put("Access-Control-Allow-Origin", "*");
                serverContext.getDefaultRespHeaders().put("Access-Control-Allow-Methods",
                        "POST,GET,OPTIONS,DELETE,PUT,HEAD");
                serverContext.getDefaultRespHeaders().put("Access-Control-Allow-Headers", "x-requested-with");
                serverContext.setCrossDomain(true);
                continue;
            }
        }
        if (this.classes == null) {
            this.classes = new Class<?>[] { bootClass };
        } else {
            this.classes[this.classes.length - 1] = bootClass;
        }
    }

    /**
     * 开始映射到ServerContext
     */
    public void resolve() {
        Class<?> clazz;
        Method method;
        GetMapping getMapping = null;
        PostMapping postMaping = null;
        PutMapping putMapping = null;
        DeleteMapping deleteMapping = null;

        ResponseType responseType = null;
        CrossDomain crossDomain = null;

        String mappingKey;
        for (int j = 0; j < classes.length; j++) {
            clazz = classes[j];

            // Controller 域下所有API可能需要跨域
            boolean shouldSetCrossDomain = false;
            if (!serverContext.isCrossDomain() && clazz.getAnnotation(CrossDomain.class) != null) {
                shouldSetCrossDomain = true;
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                method = methods[i];
                getMapping = method.getAnnotation(GetMapping.class);
                postMaping = method.getAnnotation(PostMapping.class);
                putMapping = method.getAnnotation(PutMapping.class);
                deleteMapping = method.getAnnotation(DeleteMapping.class);

                responseType = method.getAnnotation(ResponseType.class);

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
                    try {
                        RequestExecutorInfo info = new RequestExecutorInfo(method, clazz.newInstance());
                        info.addHeader("Content-Type", (responseType != null ? responseType.value() : ContentTypes.JSON)
                                + "; charset=" + serverContext.charset());

                        // 跨域设置
                        if (!serverContext.isCrossDomain() && (shouldSetCrossDomain || crossDomain != null)) {
                            info.addHeader("Access-Control-Allow-Origin", "*")
                                    .addHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PUT,HEAD")
                                    .addHeader("Access-Control-Allow-Headers", "x-requested-with");
                        }
                        if (mappingKey.indexOf('{') != -1 && mappingKey.indexOf('}') != -1) {
                            StringBuilder regString = new StringBuilder();
                            StringBuilder varName = new StringBuilder();
                            char[] segment = uri.trim().toCharArray();
                            boolean recordStart = false;
                            for (int p = 0; p < segment.length; p++) {
                                if (segment[p] == '{') {
                                    recordStart = true;
                                    continue;
                                } else if (segment[p] == '}') {
                                    recordStart = false;
                                    regString.append("([^/]*)?");
                                    info.addPathVariableName(varName.toString());
                                    varName.delete(0, varName.length());
                                } else if (recordStart) {
                                    varName.append(segment[p]);
                                    continue;
                                } else {
                                    regString.append(segment[p]);
                                }
                            }
                            serverContext.regMapping(Pattern.compile(regString.toString()), info);
                        } else {
                            serverContext.mapping(mappingKey, info);
                        }
                        LOG.info("Mapping %s : %s", mappingKey, method.toGenericString());
                    } catch (InstantiationException | IllegalAccessException e) {
                        LOG.error("控制器类需要无参数构造！");
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
