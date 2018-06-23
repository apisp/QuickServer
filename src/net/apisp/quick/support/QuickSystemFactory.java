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

import net.apisp.quick.ioc.annotation.Accept;
import net.apisp.quick.ioc.annotation.Factory;

/**
 * @author Ujued
 * @date 2018-06-23 15:14:52
 */
@Factory
public class QuickSystemFactory {

    @Accept("exception.response.beforeCode")
    public byte[] beforeCode() {
        return new byte[] { 60, 97, 114, 116, 105, 99, 108, 101, 32, 115, 116, 121, 108, 101, 61, 39, 102, 111, 110,
                116, 45, 115, 105, 122, 101, 58, 50, 50, 112, 120, 59, 102, 111, 110, 116, 45, 102, 97, 109, 105, 108,
                121, 58, 32, 67, 111, 110, 115, 111, 108, 97, 115, 59, 99, 111, 108, 111, 114, 58, 32, 35, 53, 53, 53,
                59, 116, 101, 120, 116, 45, 97, 108, 105, 103, 110, 58, 32, 99, 101, 110, 116, 101, 114, 59, 109, 97,
                114, 103, 105, 110, 58, 32, 49, 48, 112, 120, 32, 48, 39, 62, 60, 101, 109, 32, 115, 116, 121, 108, 101,
                61, 39, 99, 111, 108, 111, 114, 58, 114, 101, 100, 59, 102, 111, 110, 116, 45, 119, 101, 105, 103, 104,
                116, 58, 98, 111, 108, 100, 39, 62 };
    }

    @Accept("exception.response.afterCode")
    public byte[] afterCode() {
        return new byte[] { 60, 47, 101, 109, 62, 32 };
    }

    @Accept("exception.response.afterDesc")
    public byte[] afterDesc() {
        return new byte[] { 60, 112, 32, 115, 116, 121, 108, 101, 61, 39, 102, 111, 110, 116, 45, 115, 105, 122, 101,
                58, 49, 52, 112, 120, 59, 100, 105, 115, 112, 108, 97, 121, 58, 32, 98, 108, 111, 99, 107, 59, 109, 97,
                114, 103, 105, 110, 58, 49, 48, 112, 120, 32, 48, 59, 116, 101, 120, 116, 45, 97, 108, 105, 103, 110,
                58, 32, 99, 101, 110, 116, 101, 114, 39, 62, 81, 117, 105, 99, 107, 83, 101, 114, 118, 101, 114, 47, 49,
                46, 48, 60, 47, 112, 62, 60, 47, 97, 114, 116, 105, 99, 108, 101, 62 };
    }
}