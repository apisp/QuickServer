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
import java.nio.file.Files;

import net.apisp.quick.core.WebContext;
import net.apisp.quick.core.annotation.CrossDomain;
import net.apisp.quick.core.annotation.Get;
import net.apisp.quick.core.annotation.ResponseType;
import net.apisp.quick.core.http.ContentTypes;
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
    @ResponseType(ContentTypes.HTML)
    public void html_index(HttpResponse resp) throws IOException, URISyntaxException {
        resp.body(Files.readAllBytes(
                Quicks.tactfulPath(this.getClass().getResource("/net/apisp/quick/support/html/index.html").toURI())));
    }

    @Get("/_quick/info")
    public String version(WebContext ctx) {
        return String.format("{\"version\": \"%s\", \"singletons:\": \"%s\"}", "1.6", Strings.valueOf(ctx.objects()));
    }
}
