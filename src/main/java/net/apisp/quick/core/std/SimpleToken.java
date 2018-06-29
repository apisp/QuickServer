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
package net.apisp.quick.core.std;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.apisp.quick.server.var.ServerContext;

/**
 * @author Ujued
 * @date 2018-06-27 13:05:15
 */
public class SimpleToken {
    private Map<String, String> attrbuties = new HashMap<>();
    private SimpleEncryption encryption = new SimpleEncryption();
    private String data;
    private String charset;

    public SimpleToken() {
        this(null);
    }

    public SimpleToken(String token) {
        if (ServerContext.tryGet() == null) {
            this.charset = "utf-8";
        } else {
            this.charset = ServerContext.tryGet().charset();
        }
        if (token != null) {
            try {
                data = encryption.decode(token, charset);
                String[] attrs = data.split(";");
                for (String attr : attrs) {
                    String[] kv = attr.split("=");
                    attrbuties.put(encryption.decode(kv[0], charset), encryption.decode(kv[1], charset));
                }
            } catch (UnsupportedEncodingException e) {
            }
        }

    }

    public void set(String key, String value) {
        this.attrbuties.put(key, value);
    }

    public String get(String key) {
        return this.attrbuties.get(key);
    }

    @Override
    public String toString() {
        if (data == null) {
            StringBuilder d = new StringBuilder();
            for (Map.Entry<String, String> entry : attrbuties.entrySet()) {
                try {
                    d.append(encryption.encode(entry.getKey(), charset)).append('=');
                    d.append(encryption.encode(entry.getValue(), charset));
                    d.append(';');
                } catch (UnsupportedEncodingException e) {
                }
            }
            if (d.length() > 1) {
                try {
                    data = encryption.encode(d.deleteCharAt(d.length() - 1).toString(), charset);
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        return data;
    }
}
