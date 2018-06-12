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

import net.apisp.quick.data.DataPersist;

/**
 * @author UJUED
 * @date 2018-06-12 12:18:37
 */
public class FileData implements DataPersist, Closeable {
    private static ThreadLocal<FileData> threadLocal = new ThreadLocal<>();
    private FileChannel fileChannel;

    private long fileLength = 0;

    public synchronized static final FileData prepare(Path filePath) {
        try {
            threadLocal.set(new FileData(FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.READ)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return threadLocal.get();
    }

    public static final boolean has() {
        return threadLocal.get() == null ? false : true;
    }

    public static final FileData current() {
        if (threadLocal.get() == null) {
            throw new IllegalStateException("请先为该线程准备一个" + FileData.class);
        }
        return threadLocal.get();
    }

    public FileData(FileChannel fileChannel) {
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
    public void close() throws IOException {
        this.fileChannel.close();
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

}
