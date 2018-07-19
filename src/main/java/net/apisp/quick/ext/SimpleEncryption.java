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
package net.apisp.quick.ext;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;

import net.apisp.quick.util.Strings;

/**
 * 简单的加密算法，安全性低， 仅供参考
 * 
 * @author Ujued
 * @date 2018-06-27 07:06:32
 */
public class SimpleEncryption {

    private byte key = 0;

    public SimpleEncryption() {
        for (int i = 0; i < 8; i++) {
            if (new Random().nextInt(2) == 1) {
                this.key = (byte) set(this.key, i);
            }
        }
    }

    public String bytes2String(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public byte[] string2Bytes(String hexString) {
        if (Strings.isEmpty(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public String encode(String src, String charset) throws UnsupportedEncodingException {
        return bytes2String(encode(src.getBytes(charset)));
    }

    public String decode(String code, String charset) throws UnsupportedEncodingException {
        return new String(decode(string2Bytes(code)), charset);
    }

    public byte[] encode(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            buffer.putShort(convert(b[i]));
        }
        return buffer.array();
    }

    public byte[] decode(byte[] ds) {
        ByteBuffer buffer = ByteBuffer.allocate(ds.length);
        buffer.put(ds);
        buffer.flip();
        ByteBuffer newBuffer = ByteBuffer.allocate(ds.length / 2);
        while (buffer.hasRemaining()) {
            newBuffer.put(convert(buffer.getShort()));
        }
        return newBuffer.array();
    }

    private short convert(byte a) {
        short da = 0;
        short after = (short) a;
        for (int i = 8, j = 1; i > 1; i--, j += 2) {
            da = (short) (yyy(2, 15, j) & (da |= (short) (after << i)));
            after &= xxx(2, i - 1);
            if (on(key, i)) {
                da = set(da, j + 2);
            }
        }
        da |= after;
        return da;
    }

    private byte convert(short a) {
        byte rb = 0;
        for (int i = 16; i >= 4; i -= 2) {
            if (on(a, i)) {
                rb += pow(2, (i - 2) / 2);
            }
        }
        if (on(a, 1)) {
            rb += 1;
        }
        return rb;
    }

    private int pow(int a, int b) {
        return (int) Math.pow(a, b);
    }

    private int xxx(int a, int init) {
        int sum = 0;
        for (int i = init; i >= 0; i--) {
            sum += pow(a, i - 1);
        }
        return sum;
    }

    private int yyy(int a, int max, int n) {
        int sum = 0;
        for (int i = max; i > max - n; i--) {
            sum += pow(a, i);
        }
        return sum;
    }

    private short set(short src, int bit) {
        if (!on(src, bit)) {
            src += pow(2, bit - 1);
        }
        return src;
    }

    private boolean on(short a, int i) {
        return ((a >> i - 1) & 1) == 1;
    }
}
