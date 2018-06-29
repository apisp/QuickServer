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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;

import net.apisp.quick.core.Bootable;

/**
 * @author UJUED
 * @date 2018-06-11 17:58:05
 */
public class AsyncFileWriter implements CompletionHandler<Integer, Object>, Bootable<AsyncFileWriter> {

    private long posi;
    private int ownPosi;
    private AtomicInteger watcher;
    private AsynchronousFileChannel fileChannel;
    private ByteBuffer dataBuffer;

    public AsyncFileWriter(AsynchronousFileChannel fileChannel, byte[] data, long startPosi, AtomicInteger watcher) {
        watcher.incrementAndGet();
        this.fileChannel = fileChannel;
        this.watcher = watcher;
        this.posi = startPosi;
        this.dataBuffer = ByteBuffer.wrap(data);
    }

    public AsyncFileWriter(AsynchronousFileChannel fileChannel, ByteBuffer data, long startPosi,
            AtomicInteger watcher) {
        watcher.incrementAndGet();
        this.fileChannel = fileChannel;
        this.watcher = watcher;
        this.posi = startPosi;
        this.dataBuffer = data;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        posi += result;
        ownPosi += result;
        if (dataBuffer.hasRemaining()) {
            fileChannel.write(dataBuffer, posi, attachment, this);
        }
        if (ownPosi == dataBuffer.limit()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            if (watcher.decrementAndGet() == 0) {
                try {
                    fileChannel.close();
                    System.out.println("fileChannel closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
    }

    @Override
    public AsyncFileWriter boot() {
        fileChannel.write(dataBuffer, posi, watcher, this);
        return this;
    }

}
