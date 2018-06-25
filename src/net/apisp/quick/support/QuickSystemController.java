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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;

import org.json.JSONObject;

import net.apisp.quick.core.WebContext;
import net.apisp.quick.core.annotation.CrossDomain;
import net.apisp.quick.core.annotation.Get;
import net.apisp.quick.core.annotation.Post;
import net.apisp.quick.core.annotation.View;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.core.http.HttpResponse;
import net.apisp.quick.ioc.annotation.Controller;
import net.apisp.quick.util.Quicks;
import net.apisp.quick.util.Strings;

/**
 * @author UJUED
 * @date 2018-06-12 10:35:45
 */
@Controller
@CrossDomain
public class QuickSystemController {

    @Get("/_quick.html")
    @View("/net/apisp/quick/support/html")
    public String index(HttpRequest req, HttpResponse resp) throws IOException, URISyntaxException {
        if (!SupportUtils.checkPermission(req)) {
            return "auth.html";
        }
        return "index.html";
    }

    @Post("/_quick.html")
    @View("/net/apisp/quick/support/html")
    public String login(HttpRequest req, WebContext ctx, HttpResponse resp) {
        String authKey = new String(req.body().data(0, (int) req.body().length()));
        int i = -1;
        if ((i = authKey.indexOf('=')) != -1) {
            authKey = authKey.substring(i + 1);
            if (!ctx.setting("support.access.key").equals(authKey)) {
                return "auth.html";
            }
            resp.cookie("support_access",
                    Base64.getEncoder().encodeToString(Strings.safeGetBytes(authKey, ctx.charset())));
        } else {
            return "auth.html";
        }
        return "index.html";
    }

    @Get("/_quick/info")
    public JSONObject info(WebContext ctx, HttpRequest req, HttpResponse resp) {
        if (!SupportUtils.checkPermission(req)) {
            resp.header("Content-Type", "text/plain;charset=utf-8");
            resp.body("non permission.".getBytes());
            return null;
        }
        JSONObject info = new JSONObject();
        info.put("version", Quicks.version());
        JSONObject cache = new JSONObject();
        cache.put("singletons", Strings.valueOf(ctx.objects())).put("size", ctx.objects().size());
        info.put("cache", cache);
        long allSeconds = (System.currentTimeMillis() - (Long) ctx.singleton("quickServer.startTime")) / 1000;
        int hours = (int) (allSeconds / 3600);
        short seconds = (short) (allSeconds % 3600);
        info.put("running_time", Strings.template("{}hours and {}seconds", hours, seconds));
        return info;
    }

    @Get("/_quick/prepare_ctx")
    public JSONObject prepareContext(HttpRequest req, HttpResponse resp) {
        if (!SupportUtils.checkPermission(req)) {
            resp.header("Content-Type", "text/plain;charset=utf-8");
            resp.body("non permission.".getBytes());
            return null;
        }
        return new JSONObject();
    }
}
