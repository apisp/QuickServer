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

import org.json.JSONObject;

import net.apisp.quick.core.WebContext;
import net.apisp.quick.core.annotation.CrossDomain;
import net.apisp.quick.core.annotation.Get;
import net.apisp.quick.core.annotation.View;
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
    public String index() throws IOException, URISyntaxException {
        return "index.html";
    }

    @Get("/_quick/info")
    public JSONObject info(WebContext ctx) {
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
}
