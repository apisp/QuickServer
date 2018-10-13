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
package net.apisp.quick.core.standard.http;

import net.apisp.quick.core.criterion.http.HttpCookie;

import java.util.Date;

public class StandardHttpCookie implements HttpCookie {
    private String key;
    private String value;
    private String path = "/";
    private String domain;

    private long expires = 0;

    public StandardHttpCookie(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public long expires() {
        return expires;
    }

    @Override
    public void path(String path) {
        this.path = path;
    }

    @Override
    public void expires(long expires) {
        this.expires = expires;
    }

    @Override
    public String toString() {
        StringBuilder cookieStirng = new StringBuilder();
        cookieStirng.append(this.key).append('=').append(this.value).append(';').append(' ');
        cookieStirng.append("Path=").append(this.path).append(';').append(' ');
        if (this.domain != null) {
            cookieStirng.append("Domain=").append(this.domain).append(';').append(' ');
        }
        if (this.expires != 0) {
            cookieStirng.append("Expires=").append(new Date(System.currentTimeMillis() + this.expires));
        }
        return cookieStirng.toString();
    }
}
