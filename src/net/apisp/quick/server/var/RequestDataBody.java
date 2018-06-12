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
package net.apisp.quick.server.var;

import java.nio.ByteBuffer;

import net.apisp.quick.annotation.explain.CanBeNull;
import net.apisp.quick.core.BodyBinary;
import net.apisp.quick.data.DataPersist;

/**
 * @author UJUED
 * @date 2018-06-12 11:31:34
 */
public class RequestDataBody implements BodyBinary {

    @CanBeNull
    private DataPersist reqData;

    private int bodyOffset;

    public RequestDataBody(DataPersist reqData, int bodyOffset) {
        this.reqData = reqData;
        this.bodyOffset = bodyOffset;
    }

    @Override
    public ByteBuffer data(long offset, int length) {
        if (reqData == null) {
            return null;
        }
        return ByteBuffer.wrap(reqData.data(bodyOffset + offset, length));
    }

    @Override
    public long length() {
        if (reqData == null) {
            return -1;
        }
        return reqData.dataLength();
    }

}
