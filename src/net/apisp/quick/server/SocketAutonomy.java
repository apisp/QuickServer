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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import net.apisp.quick.core.SoftCloseable;
import net.apisp.quick.data.file.FileData;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;
import net.apisp.quick.server.HttpRequestResolver.HttpRequestInfo;
import net.apisp.quick.server.var.ServerContext;
import net.apisp.quick.util.IDs;

/**
 * @author UJUED
 * @date 2018-06-12 16:05:50
 */
public class SocketAutonomy implements SoftCloseable {
    private static final Log LOG = LogFactory.getLog(SocketAutonomy.class);
    private Socket sock;
    private Long recentTime;
    private volatile boolean isInterrupted;

    public SocketAutonomy(Socket sock) {
        this.sock = sock;
        this.recentTime = System.currentTimeMillis();
    }

    public static void activeAsync(Socket sock, List<SocketAutonomy> list) {
        SocketAutonomy me = new SocketAutonomy(sock);
        list.add(me);
        DefaultQuickServer.socketAutonomyExecutor.submit((args) -> {
            SocketAutonomy sa = (SocketAutonomy) args[0];
            try {
                InputStream is = sock.getInputStream();
                byte[] first = null;
                int i = 0, n = 0;
                byte[] buf = new byte[1024 * 10];
                String reqDataFile = null;
                while (!sa.isInterrupted) {
                    if ((i = is.read(buf)) == -1) {
                        sock.close();
                        LOG.debug("Read -1, so this socket closed.");
                        break;
                    }
                    if (n == 0) {
                        first = Arrays.copyOfRange(buf, 0, i);
                    } else {
                        if (n == 1) {
                            reqDataFile = IDs.uuid();
                            FileData.create(reqDataFile, ServerContext.tryGet().getTmpPath(reqDataFile)).persist(first);
                        }
                        FileData.find(reqDataFile).persist(buf, 0, i);
                    }
                    n++; // 更新状态信息
                    if (is.available() == 0) {
                        // 请求数据接受完毕，开始响应
                        DefaultQuickServer.responseExecutor.submit((args1) -> {
                            ByteBuffer buffer = (ByteBuffer) args1[0];
                            FileData reqData = (FileData) args1[1];
                            HttpRequestInfo reqInfo = HttpRequestResolver.resolve(buffer).setReqData(reqData);
                            try {
                                OutputStream out = sock.getOutputStream();
                                HttpResponseExecutor.execute(reqInfo).response(out);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, ByteBuffer.wrap(first), FileData.find(reqDataFile));
                        sa.recentTime = System.currentTimeMillis();
                        n = 0;
                        reqDataFile = null;
                    }
                }
            } catch (SocketException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, me);
    }

    public void interrupt() {
        this.isInterrupted = true;
    }

    public long freeTime() {
        return System.currentTimeMillis() - recentTime;
    }

    @Override
    public String toString() {
        return sock.toString();
    }

    @Override
    public void close() {
        try {
            this.sock.close();
        } catch (IOException e) {
            LOG.debug(e);
        }
    }
}
