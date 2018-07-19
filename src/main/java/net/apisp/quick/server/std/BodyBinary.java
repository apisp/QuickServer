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
package net.apisp.quick.server.std;

import java.nio.ByteBuffer;

/**
 * 二进制数据体接口
 * 
 * @author Ujued
 * @date 2018-06-12 11:27:36
 */
public interface BodyBinary {
    
    /**
     * 从给定的偏移量开始读数据到给定的字节缓冲区中，字节缓冲区的容量和数据总长度，是瓶颈
     * 
     * @param offset 开始读取的偏移量
     * @param buffer 要存放的缓冲区
     */
    void data(long offset, ByteBuffer buffer);

    /**
     * 从给定的偏移量开始读给定的长度数据并返回，给定的长度大于数据剩余长度时，按剩余长度计算
     * 
     * @param offset 开始读取的偏移量
     * @param length 需要读取的长度
     * @return 读到的数据
     */
    byte[] data(long offset, int length);

    /**
     * 数据总长度
     * 
     * @return 数据总长度
     */
    long length();
}
