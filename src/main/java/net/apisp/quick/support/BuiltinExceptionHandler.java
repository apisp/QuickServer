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
package net.apisp.quick.support;

import java.util.Objects;
import java.util.Optional;

import net.apisp.quick.server.std.QuickContext;
import net.apisp.quick.core.http.ContentTypes;
import net.apisp.quick.core.http.ExceptionHandler;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.core.http.HttpResponse;
import net.apisp.quick.core.http.HttpStatus;
import net.apisp.quick.server.ServerContext;
import net.apisp.quick.support.lang.FlowControl;
import net.apisp.quick.util.Reflects;
import net.apisp.quick.util.Strings;

/**
 * 默认控制器异常处理类，响应一个500错误页面
 * 
 * @author Ujued
 * @date 2018-06-26 17:41:43
 */
public class BuiltinExceptionHandler implements ExceptionHandler {

    @Override
    public void handle(HttpRequest req, HttpResponse resp, Throwable e) {
        QuickContext serverContext = ServerContext.tryGet();
        FlowControl.get().when(Objects.isNull(serverContext)).raise(new IllegalStateException("Unseasonable!"));
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String userAgent = req.header("User-Agent");
        FlowControl.get().when(Objects.nonNull(userAgent)).and(userAgent.contains("Mozilla")).run(() -> {
            Optional<String> body = Optional.ofNullable((String) serverContext.singleton("500.html"));
            resp.body(Strings.bytes(body.orElse(String.valueOf(status.getCode())), serverContext.charset()));
        }).orRun(() -> {
            resp.body((status.getCode() + " " + status.getDesc()).getBytes());
        });
        // ReflectCall
        Reflects.invoke(resp, "setHttpStatus", status);
        resp.header("Content-Type", ContentTypes.HTML + ";charset=" + serverContext.charset());
    }

}
