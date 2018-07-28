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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import net.apisp.quick.ioc.annotation.Accept;
import net.apisp.quick.ioc.annotation.Factory;
import net.apisp.quick.template.T;
import net.apisp.quick.util.Quicks;

/**
 * @author Ujued
 * @date 2018-06-23 15:14:52
 */
@Factory
public class SupportObjectFactory {

    @Accept
    public T template() {
        return T.newT();
    }

    @Accept("404.html")
    public String notFoundHTML(T t) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("title", "404 Not Found");
            vars.put("code", 404);
            vars.put("desc", "Not Found");
            URI errURI = this.getClass().getResource("/net/apisp/quick/support/html/err.thtml").toURI();
            return t.setVariables(vars).render(Quicks.tactfulPath(errURI));
        } catch (URISyntaxException e) {
        }
        return "404 Not Found";
    }

    @Accept("400.html")
    public String badRequestHTML(T t) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("title", "400 Bad Request");
            vars.put("code", 400);
            vars.put("desc", "Bad Request");
            URI errURI = this.getClass().getResource("/net/apisp/quick/support/html/err.thtml").toURI();
            return t.setVariables(vars).render(Quicks.tactfulPath(errURI));
        } catch (URISyntaxException e) {
        }
        return "404 Bad Request";
    }

    @Accept("500.html")
    public String internalServerErrorHTML(T t) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("title", "500 Internal Server Error");
            vars.put("code", 500);
            vars.put("desc", "Internal Server Error");
            URI errURI = this.getClass().getResource("/net/apisp/quick/support/html/err.thtml").toURI();
            return t.setVariables(vars).render(Quicks.tactfulPath(errURI));
        } catch (URISyntaxException e) {
        }
        return "500 Internal Server Error";
    }
    
    @Accept("quickServer.startTime")
    public long systemStartTime() {
        return System.currentTimeMillis();
    }
}
