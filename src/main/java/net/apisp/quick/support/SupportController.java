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

import net.apisp.quick.core.criterion.MimeTypes;
import net.apisp.quick.core.criterion.http.HttpRequest;
import net.apisp.quick.core.criterion.http.HttpResponse;
import net.apisp.quick.core.criterion.http.annotation.*;
import net.apisp.quick.core.criterion.ioc.ClassScanner;
import net.apisp.quick.core.standard.ioc.Injections;
import net.apisp.quick.core.standard.ioc.SimpleClassScanner;
import net.apisp.quick.core.criterion.ioc.annotation.Autowired;
import net.apisp.quick.core.criterion.ioc.annotation.Controller;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.old.server.MappingResolver;
import net.apisp.quick.old.server.http.WebContext;
import net.apisp.quick.old.server.std.ContextEnhancer;
import net.apisp.quick.old.server.std.QuickContext;
import net.apisp.quick.util.Quicks;
import net.apisp.quick.util.Reflects;
import net.apisp.quick.util.Strings;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * @author UJUED
 * @date 2018-06-12 10:35:45
 */
@Controller
@EnableCros
public class SupportController {
	private static final Log LOG = LogFactory.getLog(SupportController.class);

	@Autowired
	private QuickContext context;

	@Autowired("quickServer.bootClass")
	private Class<?> bootClass;

	@Autowired("user.classpath.uri")
	private URI classpathURI;

	@Get("/favicon.ico")
	@View("/net/apisp/quick/support/html")
	@ResponseType(MimeTypes.ICO)
	public String favicon() {
		return "favicon.ico";
	}

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
			resp.cookie("support_access", Base64.getEncoder().encodeToString(Strings.bytes(authKey, ctx.charset())));
		} else {
			return "auth.html";
		}
		return "index.html";
	}

	@Get("/_quick/info")
	public JSONObject info(WebContext ctx, HttpRequest req, HttpResponse resp) {
		if (!SupportUtils.checkPermission(req)) {
			return new JSONObject().put("permission", "denied");
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

	@Get("/_quick/enhance_ctx")
	public JSONObject prepareContext(HttpRequest req, HttpResponse resp) {
		if (!SupportUtils.checkPermission(req)) {
			return new JSONObject().put("permission", "denied");
		}
		Set<String> pset = new HashSet<>();
		ClassScanner classScanner = SimpleClassScanner.create(classpathURI, Quicks.packageName(bootClass));
		Class<? extends ContextEnhancer>[] preparations = classScanner.getByInterface(ContextEnhancer.class);
		for (int i = 0; i < preparations.length; i++) {
			try {
				ContextEnhancer preparation = (ContextEnhancer) preparations[i].newInstance();
				Injections.inject(preparation, context);
				preparation.enhance(context);
				pset.add(preparations[i].getName());
			} catch (InstantiationException | IllegalAccessException e) {
				LOG.warn("{} is not suitable.", preparations[i]);
			}
		}
		return new JSONObject().put("preparations", Strings.valueOf(pset));
	}

	@Get("/_quick/load_controller")
	public JSONObject loadController(HttpRequest req, HttpResponse resp) {
		if (!SupportUtils.checkPermission(req)) {
			return new JSONObject().put("permission", "denied");
		}
		ClassScanner classScanner = SimpleClassScanner.create(classpathURI, Quicks.packageName(bootClass));
		Class<?>[] controllers = classScanner.getByAnnotation(Controller.class);
		context.singleton(MappingResolver.class).addControllerClasses(controllers).resolveTo(context);
		return new JSONObject().put("new_controllers", Arrays.toString(controllers));
	}

	@Delete("/_quick/singleton/{name}")
	public JSONObject unloadSingleton(@Variable("name") String name) {
		if (Reflects.invoke(context, "unloadSingleton", name)) {
			return Quicks.message(200, "ok", null, null);
		}
		return Quicks.message(500, "error", null, null);
	}
}
