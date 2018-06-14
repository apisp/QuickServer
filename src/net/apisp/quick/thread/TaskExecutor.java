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
package net.apisp.quick.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.apisp.quick.log.Log;
import net.apisp.quick.log.LogFactory;

/**
 * @author UJUED
 * @date 2018-06-13 21:24:33
 */
public class TaskExecutor {
    private static final Log LOG = LogFactory.getLog(TaskExecutor.class);
    private BlockingQueue<HangableThread> pool;
    private HangableThread[] poolRef;
    private int lastPosition;
    private volatile boolean isShutdown;
    private int poolSize;

    private TaskExecutor(String name, int initSize) {
        this.poolSize = initSize;
        this.pool = new ArrayBlockingQueue<>(initSize);
        this.poolRef = new HangableThread[initSize];
        for (int i = 0; i < initSize; i++) {
            HangableThread t = new HangableThread();
            t.setName(name + "-" + i);
            this.pool.offer(t);
            this.poolRef[i] = t;
            t.start();
        }
    }

    /**
     * 提交一个带参数的任务
     * 
     * @param task
     * @param args
     */
    public synchronized void submit(Task task, Object... args) {
        if (isShutdown) {
            throw new IllegalStateException("My life is over. Thank you!");
        }
        HangableThread thread = pool.poll();
        if (thread == null) {
            thread = poolRef[lastPosition++ % poolSize];
        }
        thread.taskQueue.offer(new TaskUnit(task, args));
    }

    /**
     * 可挂起的线程。用队列实现没有任务时挂起
     * 
     * @author UJUED
     * @date 2018-06-13 23:16:06
     */
    class HangableThread extends Thread {
        private BlockingQueue<TaskUnit> taskQueue = new ArrayBlockingQueue<>(10);

        @Override
        public void run() {
            TaskUnit task = null;
            while (!this.isInterrupted()) {
                try {
                    task = taskQueue.take();
                } catch (InterruptedException e) {
                    break;
                }
                task.getTask().run(task.getArgs());
                pool.offer(this);
                LOG.debug("Me is free now.");
            }
        }
    }

    /**
     * 向池中所有线程发送终端信号，池中的任务执行完毕后会关闭所有线程
     */
    public void shutdown() {
        this.isShutdown = true;
        for (int i = 0; i < poolSize; i++) {
            try {
                pool.take().interrupt();
            } catch (InterruptedException e) {
                // 关闭线程池的线程被中断，那无力回天，我们什么也不做
            }
        }
    }

    /**
     * 创建cpu核心数2倍大小的线程池
     * 
     * @return
     */
    public static TaskExecutor create() {
        return new TaskExecutor("quick", Runtime.getRuntime().availableProcessors() * 2);
    }

    public static TaskExecutor create(int initSize) {
        return new TaskExecutor("quick", initSize);
    }

    public static TaskExecutor create(String name, int initSize) {
        return new TaskExecutor(name, initSize);
    }
}
