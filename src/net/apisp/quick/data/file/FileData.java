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

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.apisp.quick.data.DataPersist;
import net.apisp.quick.log.Logger;

/**
 * @author UJUED
 * @date 2018-06-11 17:46:11
 */
public class FileData implements DataPersist {
    private static final Logger LOGGER = Logger.get(FileData.class);
    private AtomicLong bytePosi = new AtomicLong(0);

    private AtomicInteger watcher = new AtomicInteger();
    private AsynchronousFileChannel fileChannel;

    public FileData(Path dataFilePath) {
        try {
            this.fileChannel = AsynchronousFileChannel.open(dataFilePath, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
        } catch (AccessDeniedException e) {
            LOGGER.error("%s AccessDenied.", dataFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long persist(byte[] part) {
        new AsyncFileWriter(fileChannel, part, bytePosi.get(), watcher).boot();
        return bytePosi.addAndGet(part.length);
    }

    @Override
    public byte[] data(long startPosi, long stopPosi) {
        return null;
    }

    public boolean isFinished() {
        return watcher.get() == 1 || watcher.get() == 0 ? true : false;
    }
}
