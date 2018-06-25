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

import java.util.Base64;

import net.apisp.quick.annotation.DependOn;
import net.apisp.quick.core.http.HttpCookie;
import net.apisp.quick.core.http.HttpRequest;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.util.Strings;

/**
 * @author Ujued
 * @date 2018-06-25 21:35:13
 */
public abstract class SupportUtils {

    @DependOn("WebContext")
    public static boolean checkPermission(HttpRequest req) {
        ServerContext ctx = ServerContext.tryGet();
        Boolean on = (Boolean) ctx.setting("support.access.open");
        HttpCookie key = req.cookie("support_access");
        if (!on || ctx == null || key == null) {
            return false;
        }
        byte[] reqKey = Base64.getDecoder().decode(key.value());
        String passwd = (String) ctx.setting("support.access.key");
        if (passwd.equals(Strings.toString(reqKey, ctx.charset()))) {
            return true;
        }
        return false;
    }
}
