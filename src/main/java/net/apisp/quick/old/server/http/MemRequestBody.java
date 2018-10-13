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
package net.apisp.quick.old.server.http;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import net.apisp.quick.old.server.std.BodyBinary;

/**
 * @author UJUED
 * @date 2018-06-13 15:09:05
 */
public class MemRequestBody implements BodyBinary {

    private byte[] body;

    public MemRequestBody(byte[] body) {
        this.body = body;
    }

    @Override
    public void data(long offset, ByteBuffer buffer) {
        if (offset < 0 || offset >= body.length) {
            throw new IllegalArgumentException("Offset error.");
        }
        int cap = (int) (body.length - offset);
        if (cap > buffer.capacity()) {
            cap = buffer.capacity();
        }
        buffer.clear();
        buffer.put(body, (int) offset, cap);
        buffer.flip();
    }

    @Override
    public byte[] data(long offset, int length) {
        if (offset < 0 || offset + length > body.length) {
            throw new IllegalArgumentException("Offset or length error.");
        }
        return Arrays.copyOfRange(body, (int) offset, (int) offset + length);
    }

    @Override
    public long length() {
        return body.length;
    }

    @Override
    public String toString() {
        try {
            return new String(this.body, "utf8");
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

}
