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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author UJUED
 * @date 2018-06-11 10:09:45
 */
public class Files {
    public static File save(String path, byte[] data) {
        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(path),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            new FileCompletionHandler(fileChannel, data).start();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new File(path);
    }

    static class FileCompletionHandler implements CompletionHandler<Integer, Object> {
        static long posi = 0;
        private AsynchronousFileChannel fileChannel;
        private ByteBuffer dataBuffer;

        public FileCompletionHandler(AsynchronousFileChannel fileChannel, byte[] body) {
            this.fileChannel = fileChannel;
            dataBuffer = ByteBuffer.allocate(body.length);
            dataBuffer.put(body).flip();
        }

        @Override
        public void completed(Integer result, Object attachment) {
            posi += result;
            if (dataBuffer.hasRemaining()) {
                fileChannel.write(dataBuffer, posi, attachment, this);
            } else {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            exc.printStackTrace();
        }

        public void start() {
            fileChannel.write(dataBuffer, 0, null, this);
        }

    }
}
