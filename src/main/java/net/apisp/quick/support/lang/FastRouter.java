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
package net.apisp.quick.support.lang;

import java.util.function.Function;
import java.util.function.Supplier;

import net.apisp.quick.core.criterion.http.HttpRequest;
import net.apisp.quick.old.server.ServerContext;

/**
 * 快速映射。 在任何可以获取到QuickContext的地方，都可以用quickContext.mapping(key,
 * executor)的形式自定义URI映射。
 * 
 * @author Ujued
 * @see ServerContext
 */
public class FastRouter {

	@SuppressWarnings("unchecked")
	public Object route(HttpRequest req, Object executor) {
		Class<?> type = executor.getClass();

		if (Runnable.class.isAssignableFrom(type)) {
			((Runnable) executor).run();
		} else if (Supplier.class.isAssignableFrom(type)) {
			return ((Supplier<Object>) executor).get();
		} else if (Function.class.isAssignableFrom(type)) {
			return ((Function<HttpRequest, Object>) executor).apply(req);
		} else {
			((ArgRunnable<HttpRequest>) executor).run(req);
		}
		return null;
	}
}
