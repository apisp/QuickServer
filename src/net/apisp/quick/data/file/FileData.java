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
package net.apisp.quick.data.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.apisp.quick.data.DataPersist;

/**
 * @author UJUED
 * @date 2018-06-12 12:18:37
 */
public class FileData implements DataPersist, Closeable {
    private static final Map<String, FileData> CACHE = new ConcurrentHashMap<>();
    private FileChannel fileChannel;

    private long fileLength = 0;

    public static FileData create(Path filePath) throws IOException {
        return create(filePath.getFileName().toString(), filePath);
    }

    public static FileData create(String name, Path filePath) throws IOException {
        CACHE.put(name, new FileData(FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE)));
        return CACHE.get(name);
    }

    private FileData(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    @Override
    public long persist(byte[] part) {
        return persist(ByteBuffer.wrap(part));
    }

    @Override
    public long persist(byte[] part, int offset, int length) {
        return persist(ByteBuffer.wrap(part, offset, length));
    }

    @Override
    public long persist(ByteBuffer part) {
        try {
            fileChannel.write(part);
            part.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLength += part.limit();
    }

    public ByteBuffer buffer(long offset, int length) {
        if (offset + length > fileLength || offset < 0) {
            throw new IllegalArgumentException("Offset or length not standard.");
        }
        ByteBuffer dst = ByteBuffer.allocate(length);
        try {
            this.fileChannel.read(dst, offset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dst.flip();
        return dst;
    }

    @Override
    public void close() {
        try {
            this.fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] data(long offset, int length) {
        ByteBuffer body = buffer(offset, length);
        byte[] b = new byte[body.limit()];
        body.get(b);
        body = null;
        return b;
    }

    @Override
    public long dataLength() {
        return fileLength;
    }

    public static FileData find(String name) {
        if (name == null) {
            return null;
        }
        return CACHE.get(name);
    }

    public FileData cache(String name, FileData obj) {
        if (name == null) {
            return null;
        }
        return CACHE.put(name, obj);
    }

}
