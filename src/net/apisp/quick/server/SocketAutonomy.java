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
package net.apisp.quick.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import net.apisp.quick.annotation.explain.CanBeNull;
import net.apisp.quick.data.file.FileData;
import net.apisp.quick.server.HttpRequestResolver.HttpRequestInfo;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.util.IDs;

/**
 * @author UJUED
 * @date 2018-06-12 16:05:50
 */
public class SocketAutonomy extends Thread implements Closeable {
    private Socket sock;
    private Long recentTime;

    public SocketAutonomy(Socket sock) {
        this.sock = sock;
        this.recentTime = System.currentTimeMillis();
    }

    public static void activeAsync(Socket sock, List<SocketAutonomy> list) {
        SocketAutonomy me = new SocketAutonomy(sock);
        list.add(me);
        me.start();
    }

    @Override
    public void run() {
        try {
            InputStream is = sock.getInputStream();
            byte[] first = null;
            int i = 0, n = 0;
            byte[] buf = new byte[1024 * 10];
            while (!this.isInterrupted()) {
                if ((i = is.read(buf)) == -1) {
                    sock.close();
                    break;
                }
                if (n == 0) {
                    first = Arrays.copyOfRange(buf, 0, i);
                } else {
                    if (n == 1) {
                        FileData.prepare(ServerContext.tryGet().getTmpPath(IDs.uuid()));
                        FileData.current().persist(first);
                    }
                    FileData.current().persist(buf, 0, i);
                }
                n++; // 更新状态信息
                if (is.available() == 0) {
                    n = 0;
                    new ResponseThread(ByteBuffer.wrap(first), FileData.has() ? FileData.current() : null).start();
                    this.recentTime = System.currentTimeMillis();
                }
            }
        } catch (SocketException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long freeTime() {
        return System.currentTimeMillis() - recentTime;
    }

    @Override
    public String toString() {
        return sock.toString();
    }

    @Override
    public void close() throws IOException {
        this.sock.close();
    }

    /**
     * 响应线程
     * 
     * @author UJUED
     * @date 2018-06-12 19:25:15
     */
    class ResponseThread extends Thread {
        private ByteBuffer buffer;

        @CanBeNull
        private FileData reqData;

        public ResponseThread(ByteBuffer buffer, FileData reqData) {
            this.buffer = buffer;
            this.reqData = reqData;
        }

        @Override
        public void run() {
            HttpRequestInfo reqInfo = HttpRequestResolver.resolve(buffer).setReqData(reqData);
            try {
                OutputStream out = sock.getOutputStream();
                HttpResponseExecutor.execute(reqInfo).response(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
